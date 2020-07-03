import com.github.mm.coloredconsole.colored
import kotlinx.coroutines.*
import org.clintrorick.*
import java.io.File
import kotlin.random.Random

fun playAndEvalSingleGame(){
        val gameState = initGameState()

    //TODO start by training player 1 against a random player 2
        //TODO move up to player 2 using buy provinces first
        //TODO move up to player 2 using big money strat

        //TODO instead of static player 2
        // strategies at some point have player 2 follow our established Q table policy
        val playerWhoWon = playSingleGame(gameState)
        if (playerWhoWon == "player1"){
            win_count.incrementAndGet()
        }else{
            loss_count.incrementAndGet()
        }
    }

    fun runGame() {
        //run in batches 1000 of 8
        runBlocking(Dispatchers.Default) {
            for (epochCount in 1..number_of_epochs) {
                have_we_printed_actions_this_epoch = false
                val batchesDeferred : MutableList<Deferred<Unit>> = mutableListOf()
                for (batchCount in 1..batches_per_epoch) { //1 batch per thread theoretically
                    batchesDeferred.add(
                        async(start = CoroutineStart.DEFAULT) {
                            for (x in 1..games_per_batch) {
                                playAndEvalSingleGame()
                            }
                        })
                }
                batchesDeferred.forEach {
                    it.await()
                }
                batchesDeferred.clear()

                endOfEpochLogsAndUpdates(epochCount)
            }
        }
    }

    fun playSingleGame(gameState : GameState) : String{
        //delay(1)
        //println("[${Thread.currentThread().name}] playing single game")
        val rand : Int = Random.nextInt(5000)
        val printOutPlayerActionsThisGame : Boolean
        printOutPlayerActionsThisGame = rand == 142 && !have_we_printed_actions_this_epoch
        if (printOutPlayerActionsThisGame){
            println("logging actions this game")
            File("output.txt").appendText("starting new game" + System.lineSeparator() + System.lineSeparator())

        }
        val statesAndActionsTraversedByP1ThisEpisode : MutableList<Pair<String,PlayerAction>>  = mutableListOf()
        for (x in 1..1000) {

            //base64 point totals in tens
            val serializedState = serializeGameStateForQTable(gameState)
            val nextStateAndThisAction : Pair<GameState,PlayerAction>
                    = nextGameAction(gameState, printOutPlayerActionsThisGame)
            gameState.numActionsTaken++
            if (gameState.player1State.isItMyTurn) {
//                    println("player1ThisAction:${nextStateAndThisAction.second}".cyan)
//                    println("player1NextState:${nextStateAndThisAction.first}".blue)

                //the state returned is the state AFTER the action was taken
                // - need to return the state prior to the action being taken
                statesAndActionsTraversedByP1ThisEpisode.add(
                    Pair(serializedState, nextStateAndThisAction.second)
                )

            }

            if (isGameOver(gameState)) {
                gamesPlayedInTotal.incrementAndGet()

                val p1points = pointTotal( gameState.player1State )
                val p2points = pointTotal( gameState.player2State )
                if (gameState.numActionsTaken < 200) {
                    updateStateActionReturnsBellman(
                        statesAndActionsTraversedByP1ThisEpisode, calculateEndOfGameReward(p1points, p2points)
                    )
                }

                return if (p1points > p2points) "player1" else "player2"

            }
        }
        discarded_episodes.incrementAndGet()
        return "no one won"
        // println("game went to time - 1000 actions - no learning")
        //printPoints(gameState)
        //println(gameState)
    }

    //returns playeraction and previous gamestate and gamestate after action applied
    fun nextGameAction(gameState : GameState, printOutPlayerActions : Boolean) : Pair<GameState,PlayerAction> {

        number_of_actions_taken_across_all_epochs.incrementAndGet()

        val availableActions = determineAvailableActions(gameState)
//        colored {
//            //println("Available actions $availableActions".green)
//        }

        //determined action - exploration set to 100%
        lateinit var playerAction : PlayerAction
        if (gameState.player1State.isItMyTurn){

            if (printOutPlayerActions){
                have_we_printed_actions_this_epoch = true
            }
            //make sure to use
            playerAction = player_1_strategy(availableActions, gameState, qTable, printOutPlayerActions)
        }else{
            playerAction = current_opponent_strategy(availableActions, gameState, qTable)
        }

        applyPlayerActionToGameState(playerAction, gameState)
        //checkForInvalidGameState(gameState)

        colored {
            //println("------------")
            //println("current player state: ${getActivePlayer(gameState)} )".cyan)
            //println("------------")
        }

        return Pair(gameState,playerAction) // return for long-term tracking purposes

    }


    private fun isGameOver(gameState : GameState) : Boolean {

        if (gameState.marketState.cardStacks.count { it.countOfCard == 0 } >= 3){
            return true
        }

        if (gameState.marketState.cardStacks.find{ it.card.cardName == PROVINCE_CARD_NAME }?.countOfCard == 0){
            return true
        }

        if (gameState.numActionsTaken > 200){
            return true
        }

        return false
}



