fun serializeGameStateForQTable(gameState : GameState) :String{
    return toStringPointsForPlayer1(gameState) + "-"
        .plus(toStringPointsForPlayer2(gameState))
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count { it.cardName === VILLAGE_CARD_NAME })
        .plus("V")
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count { it.cardName === SMITHY_CARD_NAME })
        .plus("S")
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count { it.cardName === MARKET_CARD_NAME })
        .plus("M")
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count { it.cardName === PROVINCE_CARD_NAME })
        .plus("P")
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count { it.cardName === DUCHY_CARD_NAME })
        .plus("D")
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count {it.cardName == SILVER_CARD_NAME })
        .plus("Slv")
        .plus(allCardsInHandDeckPlayDiscard(gameState.player1State).count { it.cardName == GOLD_CARD_NAME })
        .plus("Gld")


}


fun pointTotal(playerState : PlayerState) : Int {

    return allCardsInHandDeckPlayDiscard(playerState).sumBy { it.hasVPs }

}
