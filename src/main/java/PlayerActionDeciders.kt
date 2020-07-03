import org.clintrorick.*
import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.sqrt
import kotlin.random.Random


//TODO UCB to make the learning process prioritize unseen paths?

    val decidePlayerActionRandomly = fun (availableActions: Set<PlayerAction>, gs: GameState, qTable : QTable) : PlayerAction {
        try {
            if (availableActions.size == 1){
                return availableActions.first()
            }else{
                return availableActions.toList()[Random.nextInt(until = availableActions.size)]
            }
        }catch( e :Exception ){
            println(availableActions)
            throw RuntimeException()
        }
    }

    val decidePlayerActionViaPureGreed = fun (availableActions: Set<PlayerAction>,
                                              gs: GameState,
                                              qTable : QTable ,
                                              printAction : Boolean ): PlayerAction {

        val actionToQMap : MutableMap<PlayerAction,Double> = mutableMapOf()

        availableActions.forEach {
            val qLookup : Double = qTable.getQValueForStateAction(serializeGameStateForQTable(gs) +  it.toString())
            actionToQMap[it] = qLookup
        }

        exploit_count.incrementAndGet()
        val selectedAction = actionToQMap.maxBy { it.value }?.key ?: throw Exception("no available actions")
        if (printAction){
            File("output.txt").appendText(
                selectedAction.toString()
                        + System.lineSeparator()
                        + System.lineSeparator()
            )
        }
        return selectedAction


    }


    val decidePlayerActionViaUCB = fun (
        availableActions: Set<PlayerAction>,
        gs: GameState,
        qTable : QTable ,
        printAction : Boolean ) : PlayerAction {


        val actionToQMap : MutableMap<PlayerAction,QTableEntry> = mutableMapOf()
        val actionToNumPullsMap : MutableMap<PlayerAction,QTableEntry> = mutableMapOf()
        availableActions.forEach {

            val qLookup : QTableEntry = qTable.getQTable()[serializeGameStateForQTable(gs) +  it.toString()]
                                                    ?: QTableEntry(0F.toDouble(),0)

            actionToQMap[it] = qLookup

        }

        if (availableActions.size == 1){
            num_one_action_available_to_choose.incrementAndGet()
            return availableActions.toList()[0]
        }

        val sumPullsForAllOfTheseActionPossibilities = actionToQMap.values.sumBy { it.numPullsOverTraining }


        val selectedActionEntry : Map.Entry<PlayerAction,QTableEntry> = actionToQMap.maxBy { //sigmoid0To1(
                                                        ucbAlgo(
                                                            it.value.qValue,
                                                            sumPullsForAllOfTheseActionPossibilities,
                                                            it.value.numPullsOverTraining)/*)*/ }
                                                                    ?: throw Exception("no available actions")

        val  maxQActionEntry : Map.Entry<PlayerAction,QTableEntry> = actionToQMap.maxBy { it.value.qValue }
            ?: throw Exception("no available actions")



        if (maxQActionEntry.value.qValue == 0F.toDouble()){
            all_new_state_actions_this_epoch.incrementAndGet()
        } else if (selectedActionEntry.key == maxQActionEntry.key){
            num_true_exploit_actions_this_epoch.incrementAndGet()
        }else{
            num_explore_actions_this_epoch.incrementAndGet()
        }


        if (printAction){
            File("output.txt").appendText(
                selectedActionEntry.key.toString()
                        + System.lineSeparator()
                        + System.lineSeparator()
            )
        }

        return selectedActionEntry.key

    }

    //use potential learning rate to determine action selection? This kind of already does


    fun sigmoid0To1(x : Double) : Double {
        return 1 / (1 + exp(-1*x))
    }

    val decidePlayerActionViaEpsilonPoint1 =fun(availableActions: Set<PlayerAction>,
                                            gs: GameState,
                                            qTable : QTable,
                                            printAction : Boolean) : PlayerAction{
    val rand : Double = Random.nextInt(100) / 100F.toDouble()
        if (rand > 0.10F) {

            val actionToQMap : MutableMap<PlayerAction,Double> = mutableMapOf()

            availableActions.forEach {
                val qLookup : Double = qTable.getQValueForStateAction(serializeGameStateForQTable(gs) +  it.toString())
                actionToQMap[it] = qLookup
            }

            if (actionToQMap.values.filter{it > 0F}.isEmpty()){
                explore_count.incrementAndGet()

                return decidePlayerActionRandomly(availableActions, gs, qTable)// no previously visited positive actions for this state
            }
            exploit_count.incrementAndGet()
            val selectedAction = actionToQMap.maxBy { it.value }?.key ?: throw Exception("no available actions")
            if (printAction){
                File("output.txt").appendText(
                    selectedAction.toString()
                            + System.lineSeparator()
                            + System.lineSeparator()
                )
            }
            return selectedAction


        }else{

            explore_count.incrementAndGet()
            return decidePlayerActionRandomly(availableActions, gs, qTable)

        }
    }

    val decidePlayerActionViaEpsilon = fun (
                availableActions: Set<PlayerAction>,
                gs: GameState,
                qTable : QTable,
                printAction : Boolean ) : PlayerAction {

        val rand : Double = Random.nextInt(100) / 100F.toDouble()
        if (rand > epsilon) {

            val actionToQMap : MutableMap<PlayerAction,Double> = mutableMapOf()

            availableActions.forEach {
                val qLookup : Double = qTable.getQValueForStateAction(serializeGameStateForQTable(gs) +  it.toString())
                actionToQMap[it] = qLookup
            }

            if (actionToQMap.values.filter{it > 0F}.isEmpty()){
                explore_count.incrementAndGet()

                return decidePlayerActionRandomly(availableActions, gs, qTable)// no previously visited positive actions for this state
            }
            exploit_count.incrementAndGet()
            val selectedAction = actionToQMap.maxBy { it.value }?.key ?: throw Exception("no available actions")
            if (printAction){
                File("output.txt").appendText(
                    selectedAction.toString()
                            + System.lineSeparator()
                            + System.lineSeparator()
                )
            }
            return selectedAction


        }else{

            explore_count.incrementAndGet()
            return decidePlayerActionRandomly(availableActions, gs, qTable)

        }
    }

    val decidePlayerActionViaProvinceBuyer = fun (availableActions: Set<PlayerAction>, gs: GameState, qTable : QTable) : PlayerAction {

        if(isCardBuyAvailable(PROVINCE_CARD_NAME, availableActions)){
            return cardBuy(PROVINCE_CARD_NAME, availableActions)
        }

        return decidePlayerActionRandomly(availableActions, gs, qTable)

    }

    val decidePlayerActionViaGoldAndProvinceBuyer = fun (availableActions: Set<PlayerAction>, gs: GameState, qTable : QTable) : PlayerAction {

        if(isCardBuyAvailable(PROVINCE_CARD_NAME, availableActions)){
            return cardBuy(PROVINCE_CARD_NAME, availableActions)
        }

        if(isCardBuyAvailable(GOLD_CARD_NAME, availableActions)){
            return cardBuy(GOLD_CARD_NAME, availableActions)
        }

        return decidePlayerActionRandomly(availableActions, gs, qTable)

    }

    val decidePlayerActionViaSilverAndGoldAndProvinceBuyer = fun (availableActions: Set<PlayerAction>, gs: GameState, qTable : QTable) : PlayerAction {

        if(isCardBuyAvailable(PROVINCE_CARD_NAME, availableActions)){
            return cardBuy(PROVINCE_CARD_NAME, availableActions)
        }

        if(isCardBuyAvailable(GOLD_CARD_NAME, availableActions)){
            return cardBuy(GOLD_CARD_NAME, availableActions)
        }

        if(isCardBuyAvailable(SILVER_CARD_NAME, availableActions)){
            return cardBuy(SILVER_CARD_NAME, availableActions)
        }

        if (availableActions
                .filterIsInstance<PlayAllTreasuresPlayerAction>()
                .isNotEmpty()){
            return PlayAllTreasuresPlayerAction()
        }

        if (availableActions
                .filterIsInstance<PassTurnPlayerAction>()
                .isNotEmpty()){
            return PassTurnPlayerAction()
        }

        return PassTurnPlayerAction()

        return decidePlayerActionRandomly(availableActions, gs, qTable)

    }

    val decideViaActionDeckStrategy = fun(availableActions: Set<PlayerAction>, gs: GameState, qTable:QTable) : PlayerAction {

        if(isCardBuyAvailable(PROVINCE_CARD_NAME, availableActions)){
            return cardBuy(PROVINCE_CARD_NAME, availableActions)
        }

        if(isCardBuyAvailable(GOLD_CARD_NAME, availableActions)){
            return cardBuy(GOLD_CARD_NAME, availableActions)
        }
        if (allCardsInHandDeckPlayDiscard(getActivePlayer(gs)).count{it.cardName==SMITHY_CARD_NAME} < 3){
            if(isCardBuyAvailable(SMITHY_CARD_NAME, availableActions)){
                return cardBuy(SMITHY_CARD_NAME, availableActions)
            }
        }

        if (allCardsInHandDeckPlayDiscard(getActivePlayer(gs)).count{it.cardName==VILLAGE_CARD_NAME} < 3){
            if(isCardBuyAvailable(VILLAGE_CARD_NAME, availableActions)) {
                return cardBuy(VILLAGE_CARD_NAME, availableActions)
            }
        }

        if(isCardBuyAvailable(SILVER_CARD_NAME, availableActions)){
            return cardBuy(SILVER_CARD_NAME, availableActions)
        }

        return decidePlayerActionRandomly(availableActions, gs, qTable)

    }

    fun isCardBuyAvailable(cardName : String, availableActions : Set<PlayerAction>) : Boolean {
        return availableActions
                .filterIsInstance<BuyCardPlayerAction>()
                .any { it.card.cardName == cardName }
    }

    fun cardBuy(cardName : String, availableActions : Set<PlayerAction>) : PlayerAction {
        return availableActions
                    .filterIsInstance<BuyCardPlayerAction>()
                    .find { it.card.cardName == cardName } ?: throw Exception()
    }

