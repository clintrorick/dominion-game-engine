fun initGameState() : GameState{

    val gameState = GameState(

        PlayerState(
            inDeck = startingDeck(),
            inDiscard = mutableListOf(),
            inHand = mutableListOf(),
            inPlay = mutableListOf(),
            currentBuys = 1,
            currentMoney = 0,
            currentNumActions = 1,
            isItMyTurn = true),

        PlayerState(
            inDeck = startingDeck(),
            inDiscard = mutableListOf(),
            inHand = mutableListOf(),
            inPlay = mutableListOf(),
            currentBuys = 1,
            currentMoney = 0,
            currentNumActions = 1,
            isItMyTurn = false),

        MarketState( mutableListOf(
            CardStack( Province(),8),
            CardStack( Duchy(),8),
            CardStack( Estate(),8),
            CardStack( Gold(),30),
            CardStack( Silver(),30),
            CardStack( Copper(),0),
            CardStack( Village(),10),
            CardStack( Market(),10),
            CardStack( Smithy(),10)
        )
        ),
        0
    )
    for (x in 1..5) {
        drawCardForPlayer(gameState.player1State)
        drawCardForPlayer(gameState.player2State)
    }
    return gameState
}

fun startingDeck() : MutableList<Card> {
    return mutableListOf(Copper(),Copper(),Copper(),Copper(),Copper(),Copper(),Copper(),Estate(),Estate(),Estate())
}
