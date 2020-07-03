fun checkForInvalidPlayerState(ps:PlayerState){
    if (ps.currentMoney < 0){
        throw Exception("current money < 0")
    }
    if (ps.currentBuys < 0){
        throw Exception("current buys < 0")
    }
    if (ps.currentNumActions < 0){
        throw Exception("current numactions < 0")
    }
    if (ps.inDiscard.size < 0){
        throw Exception("inDiscard < 0")
    }
    if (ps.inHand.size < 0){
        throw Exception("inHand < 0")
    }
    if (ps.inPlay.size < 0){
        throw Exception("inPlay < 0")
    }
    if (ps.inDeck.size < 0){
        throw Exception("inDeck < 0")
    }
}

fun allCardsInHandDeckPlayDiscard(ps:PlayerState) : MutableList<Card> {
    val mList = mutableListOf<Card>()
    mList.addAll(ps.inDiscard)
    mList.addAll(ps.inDeck)
    mList.addAll(ps.inHand)
    mList.addAll(ps.inPlay)
    return mList
}

fun checkForInvalidGameState(gameState : GameState){
    //any negative integer
    checkForInvalidPlayerState(gameState.player1State)
    checkForInvalidPlayerState(gameState.player2State)
    assert(gameState.marketState.cardStacks.count{ it.countOfCard<0 } <= 3)
    assert(gameState.player1State.currentMoney >= 0)
    assert(8 == countOfCardAcrossAllPiles("Prv", gameState))
    assert(8 == countOfCardAcrossAllPiles("Estt", gameState))
    assert(8 == countOfCardAcrossAllPiles("Dchy", gameState))
    assert(10 == countOfCardAcrossAllPiles("Vil", gameState))
    assert(10 == countOfCardAcrossAllPiles("Mrkt", gameState))
    assert(10 == countOfCardAcrossAllPiles("Smthy", gameState))

    //both players turn at once

    //more copies of a card than it started with

}

fun countOfCardAcrossAllPiles(cardName:String, gameState : GameState) : Int{
    val countOfCardInMarket =   gameState.marketState.cardStacks
        .find{it.card.cardName == cardName}
        ?.countOfCard ?: throw Exception()

    return allCardsInHandDeckPlayDiscard(gameState.player1State)
        .filter{ it.cardName == cardName}
        .count() + allCardsInHandDeckPlayDiscard(gameState.player2State)
        .filter{ it.cardName == cardName}
        .count() + countOfCardInMarket
}


fun applyPlayerActionToGameState( playerAction : PlayerAction, gameState : GameState ) : GameState {

    val activePlayer = getActivePlayer(gameState)

    if (playerAction is PlayAllTreasuresPlayerAction){
        activePlayer.inHand.filter { it.hasCardTypes.contains(CardType.Treasure) }
            .forEach{
                activePlayer.inPlay.add(it)
                activePlayer.inHand.removeAt( activePlayer.inHand.indexOf( it ) )
                activePlayer.currentMoney += it.providesMoney
            }
    }
    if (playerAction is BuyCardPlayerAction){
        val buyCardAction : BuyCardPlayerAction = playerAction

        activePlayer.currentMoney -= buyCardAction.card.costToBuy
        activePlayer.currentBuys--

        val cardStack =
                gameState
                    .marketState
                            .cardStacks
                                .find { it.card.cardName == playerAction.card.cardName }
                                        ?: throw Exception("card stack not found")

        cardStack.countOfCard--

        activePlayer.inDiscard.add(cardStack.card) //TODO do we need an object copy or is this fine?

    }

    if (playerAction is PlayCardPlayerAction){
        if (playerAction.card.hasCardTypes.contains(CardType.Action)) {
            activePlayer.currentNumActions--
        }
        activePlayer.currentNumActions += playerAction.card.providesActions
        activePlayer.currentMoney += playerAction.card.providesMoney
        activePlayer.currentBuys += playerAction.card.providesBuys

        if (playerAction.card.providesCards > 0) {
            for (x in 1..playerAction.card.providesCards) {
                drawCardForPlayer(activePlayer)
            }
        }

        activePlayer.inHand.removeAt( activePlayer.inHand.indexOf( playerAction.card ) )//remove first instance of card

        activePlayer.inPlay.add(playerAction.card)

    }

    if (playerAction is PassTurnPlayerAction || playerAction is SkipBuysAndEndTurnPlayerAction){
        activePlayer.inDiscard.addAll(activePlayer.inHand)
        activePlayer.inDiscard.addAll(activePlayer.inPlay)

        activePlayer.inHand.removeAll { true }
        activePlayer.inPlay.removeAll { true }
        //discard cards in play and hand
        //execute 5 draw card actions
        for (x in 1..5){
            drawCardForPlayer(activePlayer)
        }

        activePlayer.currentBuys = 1
        activePlayer.currentMoney = 0
        activePlayer.currentNumActions = 1
        switchWhoseTurnItIs(gameState)

    }

    return gameState

}

private fun switchWhoseTurnItIs(gamestate : GameState){
    if (gamestate.player2State.isItMyTurn){
        gamestate.player1State.isItMyTurn = true
        gamestate.player2State.isItMyTurn = false
    }else if (gamestate.player1State.isItMyTurn){
        gamestate.player1State.isItMyTurn = false
        gamestate.player2State.isItMyTurn = true
    }
}


fun getActivePlayer(gameState : GameState) : PlayerState {
    return listOf(gameState.player1State, gameState.player2State)
                .find {it.isItMyTurn}
                    ?: throw RuntimeException("No player is the active player!")
}



private fun putDiscardIntoDeck(playerState : PlayerState){
    playerState.inDeck.addAll(playerState.inDiscard)
    playerState.inDiscard.removeAll{true}
}

private fun shuffleDeck(playerState :PlayerState){
    playerState.inDeck.shuffle()
}

fun shuffleDiscardIntoDeck(playerState : PlayerState){
    putDiscardIntoDeck(playerState)
    shuffleDeck(playerState)
}

fun drawCardForPlayer( playerState : PlayerState ){
    if (playerState.inDeck.isEmpty()){
        if (playerState.inDiscard.isNotEmpty()){
            //if no cards in discard, but cards in deck, reshuffle discard into deck
            shuffleDiscardIntoDeck(playerState)
        }else{
            //if no cards in discard, and no cards in deck, do nothing
            return
        }
    }

    shuffleDeck(playerState)//always shuffle before drawing in case ordering for base64 encoding goes awry via programming error - will break future cards like Harbinger
    playerState.inHand.add(playerState.inDeck[0])

    playerState.inDeck.removeAt(0)
    //remove 0-index card from deck
    //add card to hand

}

