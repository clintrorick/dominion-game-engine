import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*
interface PlayerAction


class QTable{

    val states_actions_returns : MutableMap< String , QTableEntry > = mutableMapOf()

    fun getQValueForStateAction(serializedStateAction : String) : Double {
        return states_actions_returns[serializedStateAction]?.qValue ?: 0F.toDouble()
    }

    fun getNumPullsOverAllTrainingForStateAction(serializedStateAction : String) : Int {
        return states_actions_returns[serializedStateAction]?.numPullsOverTraining ?: 0
    }

    fun getQTable() : MutableMap<String, QTableEntry >{
        return states_actions_returns;
    }
}

data class QTableEntry(var qValue: Double, var numPullsOverTraining : Int)


class PlayCardPlayerAction(val card : Card) : PlayerAction {
    override fun toString() : String {
        return "Ply$card"
    }
    override fun equals(other : Any?) : Boolean {
        return other is PlayCardPlayerAction && other.card.cardName == card.cardName

    }
}

class BuyCardPlayerAction(val card : Card) : PlayerAction {
    override fun toString() : String {
        return "Buy$card"
    }

    override fun equals(other : Any?) : Boolean {
        return other is BuyCardPlayerAction && other.card.cardName == card.cardName
    }

}

class PlayAllTreasuresPlayerAction: PlayerAction {
    override fun toString() : String {
        return "PlyAllTrz"
    }

    override fun equals(other : Any?) : Boolean {
        return other is PlayAllTreasuresPlayerAction
    }
}

class SkipBuysAndEndTurnPlayerAction : PlayerAction {
    override fun toString() : String {
        return "SkipBuysPassTurn"
    }

    override fun equals(other : Any?) : Boolean {
        return other is SkipBuysAndEndTurnPlayerAction
    }
}

class PassTurnPlayerAction : PlayerAction{
    override fun toString() : String {
        return "PassTurn"
    }

    override fun equals(other : Any?) : Boolean {
        return other is PassTurnPlayerAction
    }
}

enum class CardType{
    Action,
    Treasure,
    Victory
}

@JsonDeserialize
interface Card {
    val cardName : String
    val providesActions:Int
    val providesMoney:Int
    val providesBuys: Int
    val providesCards : Int
    val hasVPs : Int
    val costToBuy : Int
    val hasCardTypes: Set<CardType>
}

fun playerStateToQTableFormat(ps:PlayerState) : PlayerState {
    //make sure that order of cards in any zone doesn't matter to the q-table
    //by sorting them consistently

    ps.inHand.sortBy { it.cardName }
    ps.inDeck.sortBy { it.cardName }
    ps.inDiscard.sortBy { it.cardName }
    ps.inPlay.sortBy { it.cardName }
    return ps

}

fun marketStateToQTableFormat(ms:MarketState) : MarketState {
    //make sure that order of cards in any zone doesn't matter to the q-table
    //by sorting them consistently

    ms.cardStacks.sortBy{it.card.cardName}
    return ms

}

fun gameStateToQTableFormat(gs:GameState) : GameState{
     return GameState(player1State = playerStateToQTableFormat(gs.player1State).copy(),
                player2State = playerStateToQTableFormat(gs.player2State).copy(),
                marketState = marketStateToQTableFormat(gs.marketState),
                numActionsTaken = 42)


}

data class PlayerState(var inDeck : MutableList<Card>,
                       var inDiscard : MutableList<Card>,
                       var inHand : MutableList<Card>,
                       var inPlay : MutableList<Card>,
                       var currentMoney: Int,
                       var currentNumActions : Int,
                       var currentBuys : Int,
                       var isItMyTurn : Boolean)

data class MarketState(val cardStacks : MutableList<CardStack>)

data class GameState(val player1State : PlayerState,
                     val player2State : PlayerState,
                     val marketState : MarketState,
                     var numActionsTaken : Int)

data class CardStack(val card : Card, var countOfCard : Int)

fun gameStateToJson(gs: GameState) : String{
    return ObjectMapper().writeValueAsString(gs)
}
fun gameStateFromJson(gs : String) : GameState {
    return ObjectMapper().readValue(gs, GameState::class.java)
}

fun base64EncodeGameState(gs : GameState) : String{
    return Base64.getEncoder().encodeToString(gameStateToQTableFormat(gs).toString().toByteArray())
}

fun toStringPointsForPlayer1(gs : GameState) : String {
    //return Base64.getEncoder().encodeToString(pointTotal(gameStateToQTableFormat(gs).player1State).div(5).toString().toByteArray())
    return pointTotal(gs.player1State).div(10).toString()

}

fun toStringPointsForPlayer2(gs : GameState) : String {
//    return Base64.getEncoder().encodeToString(pointTotal(gameStateToQTableFormat(gs).player2State).div(5).toString().toByteArray())
        return pointTotal(gs.player2State).div(10).toString()

}

fun base64EncodePlayerHand(gs : GameState) : String{
    gs.player1State.inHand.sortBy { it.cardName }
    return Base64.getEncoder().encodeToString(gs.player1State.inHand.toString().toByteArray())
}

