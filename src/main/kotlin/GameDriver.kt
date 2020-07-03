package org.clintrorick

import QTable
import decidePlayerActionRandomly
import decidePlayerActionViaEpsilonPoint1
import decidePlayerActionViaGoldAndProvinceBuyer
import decidePlayerActionViaProvinceBuyer
import decidePlayerActionViaPureGreed
import decidePlayerActionViaSilverAndGoldAndProvinceBuyer
import decidePlayerActionViaUCB
import runGame
import java.lang.Double.isNaN
import java.lang.Math.pow
import java.lang.String.format
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*
import kotlin.system.measureTimeMillis

    //TODO for multithread - come up with a way to make hyperparams (primarily EPSILON but maybe learning rate too) constant within an epoch
    // maybe all threads wait for epoch to complete before starting anew?

    /** constant hyperparams **/

    const val discount_rate = .99F
    const val number_of_epochs = 1000
    const val batches_per_epoch = 16 // = number of coroutines
    const val games_per_batch = 20000


    const val learning_rate_decay = 1F // 1 = no decay
    const val learning_rate_floor = .5F

    fun bellmanBackup(oldQSA : Double, dynamic_learning_rate : Double, rewardTplus1 : Double, qsaPlus1 : Double) : Double{
        return oldQSA + dynamic_learning_rate * ( rewardTplus1 + discount_rate * qsaPlus1 - oldQSA )
    }

    fun dynamicLearningRate(numTimesThisActionPulled : Int) : Double {
       val returnDub = 1 / (log(numTimesThisActionPulled.toDouble(),10F.toDouble()) +1)
        if (isNaN(returnDub)){
            throw Exception("NaN")
        }
        return returnDub
    }

    fun ucbAlgo(q : Double, countOfParentStateEncountered : Int, countThisStateActionTaken : Int) : Double {
        //TODO change the explore factor based on learning delta - if delta is too low, explore more
        val exploreFactor = sqrt( 10 * ln(countOfParentStateEncountered.toDouble())  / countThisStateActionTaken.toDouble())
        return q + exploreFactor
//todo decay exploration factor over time
        /** q + sqrt(2*ln(n)/nI)  // where n = number of actions selected over all training,
         *   and nI = number of times this state/action taken over all training**/
    }


//learning rate = number of steps per episode / t, where t = state-action pair being updated for the t-th time???
    /** hyperparams mutated after each epoch **/
    //TODO have strategies accept hyperparams specific to them

    //only used if not using dynamic learning rate
    var learning_rate_initial = .2F.toDouble()
    var epsilon = .99F //percent that we will explore (act randomly) rather than exploit (take action with best Q value) - will decay over time

    var win_rate_last_epoch = 0F.toDouble()
    var have_we_printed_actions_this_epoch = false

    var player_1_strategy = decidePlayerActionViaUCB
    var player_1_strategy_name = ::decidePlayerActionViaUCB.name

    fun ucbUpdates(epochCount : Int){
        if (epochCount % go_greedy_each_x_epochs == 0){
            player_1_strategy = decidePlayerActionViaPureGreed
            player_1_strategy_name = ::decidePlayerActionViaPureGreed.name
        }else{
//            if (player_1_strategy_name == ::decidePlayerActionViaUCB.name){
//                player_1_strategy = decidePlayerActionViaEpsilonPoint1
//                player_1_strategy_name = ::decidePlayerActionViaEpsilonPoint1.name
//            } else if (player_1_strategy_name == ::decidePlayerActionViaEpsilonPoint1.name
//                        || player_1_strategy_name == ::decidePlayerActionViaPureGreed.name ){
                player_1_strategy = decidePlayerActionViaUCB
                player_1_strategy_name = ::decidePlayerActionViaUCB.name
//            }
        }
    }

    var current_opponent_strategy = decidePlayerActionViaSilverAndGoldAndProvinceBuyer
    var current_opponent_strategy_name = ::decidePlayerActionViaSilverAndGoldAndProvinceBuyer.name


    fun calculateEndOfGameReward(pointTotalP1: Int, pointTotalP2: Int) : Double{

//          var reward : Double = (pow(abs(pointTotalP1.toDouble() - pointTotalP2.toDouble()),2F.toDouble())) / 30F.toDouble()
//            if (isNaN(reward)){
//                throw Exception("NaN")
//            }
//        if (pointTotalP1-pointTotalP2>40){
//            return 32.toDouble()
//        }
//        if (pointTotalP1-pointTotalP2>30){
//            return 16F.toDouble()
//        }
//        if (pointTotalP1-pointTotalP2>20){
//            return 8F.toDouble()
//        }
//        if (pointTotalP1-pointTotalP2>10){
//            return 4F.toDouble()
//        }
        if (pointTotalP1-pointTotalP2>0) {
            return 1F.toDouble()
        }

            //if player 1 lost, grant a negative reward instead
//          if (pointTotalP2 > pointTotalP1){
//              return 0.0001F.toDouble()
//          }

        return 0F.toDouble()

    }

    /** mutable state shared across coroutines **/

    val qTable : QTable = QTable() //String = GameState+PlayerAction, serialized

    var win_count = AtomicInteger(0)
    var loss_count = AtomicInteger(0)
    var exploit_count = AtomicInteger(0)
    var explore_count = AtomicInteger(0)
    var discarded_episodes = AtomicInteger(0)
    var number_of_actions_taken_across_all_epochs = AtomicInteger(0)
    var gamesPlayedInTotal = AtomicInteger(0)

    var go_greedy_each_x_epochs = 10

    var num_true_exploit_actions_this_epoch = AtomicInteger(0)
    var num_explore_actions_this_epoch = AtomicInteger(0)
    var all_new_state_actions_this_epoch = AtomicInteger(0)
    var num_one_action_available_to_choose = AtomicInteger(0)

    var justEpochThingsList :JustEpochThingsList  = JustEpochThingsList(mutableListOf())

    class JustEpochThings(
        val winRate : Double,
        val exploredPercent : Double,
        val epochCount : Int,
        val percentOnlyOneAction : Double,
        val deltaWinRate : Double,
        val allNewPercent : Double){

        override fun toString() : String {
            return " Win_Rate "
                    .plus(format("%.6f", winRate))
                    .plus(" Explored_Percent ")
                    .plus( format("%.4f", exploredPercent))
                    .plus(" All_New_percent ")
                    .plus( format("%.4f", allNewPercent))
                    .plus(" Percent Only One Action ")
                    .plus( format("%.4f", percentOnlyOneAction))
                    .plus(" Epoch_Count ")
                    .plus( epochCount )
                    .plus(" Delta_Win_Rate ")
                    .plus(format("%.5f", deltaWinRate))
        }
    }



    class JustEpochThingsList(val justEpochThingsList :  MutableList<JustEpochThings>){
        override fun toString() : String {
            val sb : StringBuilder = StringBuilder()
            justEpochThingsList.forEach{
                sb.append(it.toString() + System.lineSeparator())
            }
            return sb.toString()
        }

        fun getList() : MutableList<JustEpochThings>{
            return justEpochThingsList
        }
    }


    fun main(args: Array<String>) {

        println(" games took ${measureTimeMillis { runGame() }}")

        val sortedQTable = qTable.getQTable().toList().sortedWith( compareBy { it.second.qValue } ) ;

        println("worst states--------")

        for (x in 0..10){

            println(sortedQTable[x])
        }

        val reversedQTable = sortedQTable.reversed()

//        for (x in reversedQTable){
//            println(x)
//        }

        println("best states--------")

        for (x in 0..10){
            println(reversedQTable[x])
        }

        println("exploit count $exploit_count")
        println("explore count $explore_count")

    }


    fun adaptiveSigmoid(delta : Double) : Double {
        return (1.0F / (1.0F+exp(-2F * delta))) - 0.5F
    }

    /** currently unused hyperparams **/

    val EPSILON_DECAY = .99999F
    val EPSILON_MIN = .1F //we will always explore this much - keeps learning from converging on suboptimal lines





