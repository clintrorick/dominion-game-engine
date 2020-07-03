import org.clintrorick.*
import java.io.File
import kotlin.math.abs
import kotlin.math.max

fun endOfEpochLogsAndUpdates(epochCount : Int){
        //end of epoch stuff below

        updateLearningRate()

        if (discarded_episodes.get() > 0) {
            println("Discarded episodes $discarded_episodes")
        }

        val winRateThisEpoch = getWinRateAndLog()

        val (percentExplorationThisEpoch, percentAllNewStatesThisEpoch, percentOnlyOneAction) = logAndUpdateExploitVsExplore()

        val deltaWinRate = winRateThisEpoch - win_rate_last_epoch

        justEpochThingsList.getList().add( JustEpochThings(
                                    winRate = winRateThisEpoch,
                                    exploredPercent = percentExplorationThisEpoch,
                                    allNewPercent = percentAllNewStatesThisEpoch,
                                    percentOnlyOneAction = percentOnlyOneAction,
                                    deltaWinRate = deltaWinRate,
                                    epochCount = epochCount ) )

        println("$justEpochThingsList")
        File("justepochthings.txt").writeText("$justEpochThingsList")
        //epsilonGreedyUpdateEpsilonAndLearningRate(winRateThisEpoch)

        ucbUpdates(epochCount)

        //TODO log percent of exploration done during a UCB epoch
        //TODO log/graph win percent and delta in win percent over time
        //TODO log/graph distribution of pulls on states - are we hitting the same ones too often?

        switchOpponentIfWinRateIsHighEnough(winRateThisEpoch)

        //TODO if standard deviation of the last 10 epochs is below a certain amount, increase epsilon and learning rate

        if (abs(winRateThisEpoch-win_rate_last_epoch) < 0.001 && player_1_strategy_name == ::decidePlayerActionViaEpsilonPoint1.name){
            println("learning seems to have stagnated - resetting learning rate")
            qTable.getQTable().values.forEach { it.numPullsOverTraining = max(it.numPullsOverTraining - 1000,0) }//don't reset learning rate for everything, just states we have visited not a whole lot
        }

        win_rate_last_epoch = winRateThisEpoch

        win_count.set(0)
        loss_count.set(0)

        //TODO change Reward function to use delta in points between player 1 and 2 instead of win rate? may incentivize Province buys

        //TODO delta in win percentage can help us adjust the learning rate and epsilon rate

        val sortedQTable = qTable.getQTable().toList().sortedWith(compareBy{it.second.qValue})
        val reversedQTable = sortedQTable.reversed()
        for (x in 1..5) {
            println(reversedQTable[x])
        }

        val sortedByPullsQTable = qTable.getQTable().toList().sortedWith(compareBy{it.second.numPullsOverTraining})
        val sortedByPullsQTableReversed = sortedByPullsQTable.reversed()
        for (x in 1..5) {
            println(sortedByPullsQTableReversed[x])
        }
        println("q table size ${qTable.getQTable().size}")
        println("$epochCount epochs of $number_of_epochs epochs total")
        println("${batches_per_epoch * games_per_batch * epochCount} games of ${number_of_epochs * batches_per_epoch * games_per_batch} games total")


        //TODO return array of states and actions traversed by player 1
        //TODO return whether player 1 won or lost
        //
        //TODO use bellman equation to backfill visited states with reward
        // - should we use a reward of -1 for losing?

    }

    //TODO when switching opponents, reset num pulls so learning rate gets higher?

    fun switchOpponentIfWinRateIsHighEnough(winRateThisEpoch : Double){
        if (winRateThisEpoch > 0.90F && current_opponent_strategy == decidePlayerActionRandomly) {
            current_opponent_strategy = decidePlayerActionViaGoldAndProvinceBuyer
            current_opponent_strategy_name = ::decidePlayerActionViaGoldAndProvinceBuyer.name
           // learning_rate = 0.5F
            qTable.getQTable().values.forEach { it.numPullsOverTraining = 0 }
        }
        else if (winRateThisEpoch > 0.90F && current_opponent_strategy == decidePlayerActionViaProvinceBuyer) {
            current_opponent_strategy = decidePlayerActionViaGoldAndProvinceBuyer
            current_opponent_strategy_name = ::decidePlayerActionViaGoldAndProvinceBuyer.name
           // learning_rate = 0.5F
            qTable.getQTable().values.forEach { it.numPullsOverTraining = 0 }
        }
        else if (winRateThisEpoch > 0.90F && current_opponent_strategy == decidePlayerActionViaGoldAndProvinceBuyer) {
            current_opponent_strategy = decidePlayerActionViaSilverAndGoldAndProvinceBuyer
            current_opponent_strategy_name = ::decidePlayerActionViaSilverAndGoldAndProvinceBuyer.name
           // learning_rate = 0.5F
            qTable.getQTable().values.forEach { it.numPullsOverTraining = 0 }
        }
    }

    fun epsilonGreedyUpdateEpsilonAndLearningRate(winRateThisEpoch : Double){

        //if(EPSILON_DECAY * EPSILON > EPSILON_MIN){
        //     EPSILON *=  EPSILON_DECAY
        // }

        if (winRateThisEpoch < 0.5F) {
            epsilon = 0.5F
        } else if (winRateThisEpoch > win_rate_last_epoch && epsilon == 0.1F) {
            learning_rate_initial = 0.1F.toDouble()
            epsilon = 0F
        } else if (winRateThisEpoch > win_rate_last_epoch || epsilon == 0.5F || epsilon == 0F) {
            // EPSILON /= 2F
            epsilon = 0.1F //adaptiveSigmoid(winRateThisEpoch - winRateLastEpoch)
            //we're doing well - sigmoid the epsilon
        } else {
            epsilon = 0.5F
            //we regressed - set epsilon to 0.5
        }
        //println("EPSILON after update $epsilon")

    }



    fun logAndUpdateExploitVsExplore() : Triple<Double,Double,Double> {
        val a : Double = num_explore_actions_this_epoch.get().toDouble()
        val b : Double = num_true_exploit_actions_this_epoch.get().toDouble()
        val c : Double = all_new_state_actions_this_epoch.get().toDouble()
        val d : Double = num_one_action_available_to_choose.get().toDouble()


        val percentExploration : Double = a / (1+a+b+c+d)
        val percentExploitation : Double = b / (1+a+b+c+d)
        val percentAllNewStates = c / (1+a+b+c+d)
        val percentOnlyOneActionAvailableToChoose : Double = d / (1+a+b+c+d)

//        println("percent-exploration for last epoch $percentExploration%")
        num_true_exploit_actions_this_epoch.set(0)
        num_explore_actions_this_epoch.set(0)
        all_new_state_actions_this_epoch.set(0)
        num_one_action_available_to_choose.set(0)
        return Triple(percentExploration, percentAllNewStates, percentOnlyOneActionAvailableToChoose)

    }

    fun getWinRateAndLog() : Double {
        val winRateThisEpoch = win_count.toDouble() / ( win_count.toDouble() + loss_count.toDouble() )

        println("Win percentage for $player_1_strategy_name last ${batches_per_epoch * games_per_batch}" +
                " against $current_opponent_strategy_name $winRateThisEpoch ")

        return winRateThisEpoch
    }

    fun updateLearningRate(){
        if (learning_rate_decay * learning_rate_initial > learning_rate_floor) {
            learning_rate_initial *= learning_rate_decay
        }
        // println("LEARNING RATE $learning_rate")

    }
