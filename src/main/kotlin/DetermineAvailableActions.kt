
    fun determineAvailableActions(gameState : GameState) : Set<PlayerAction> {

        if (gameState.player1State.isItMyTurn){
            return determineAvailableActionsForPlayer(gameState.player1State, gameState.marketState)
        }else{
            return determineAvailableActionsForPlayer(gameState.player2State, gameState.marketState)
        }

    }
    //TODO bring treasure plays together to assume player always plays all treasures
    private fun determineAvailableActionsForPlayer(playerState : PlayerState, marketState : MarketState) : Set<PlayerAction> {
        val playCardActions : List<PlayerAction>

        //TODO - handle when player is in play treasure phase and still has actions - in current setup, player plays all actions always
        if (playerState .inHand .filter { it.hasCardTypes.contains(CardType.Action) } .isNotEmpty() && playerState.currentNumActions > 0 ){

            playCardActions = playerState
                        .inHand
                        .filter { it.hasCardTypes.contains(CardType.Action)}
                        .map { actnCard -> PlayCardPlayerAction(actnCard) }

            if (playCardActions.isNotEmpty())
                return playCardActions.toSet()

        }

        if ( playerState .inHand .filter { it.hasCardTypes.contains(CardType.Treasure) } .isNotEmpty()){

            val playTreasureCardActions = playerState
                .inHand
                .filter { it.hasCardTypes.contains(CardType.Treasure)}
                .map { treasureCard -> PlayCardPlayerAction(treasureCard) }

            if (playTreasureCardActions.isNotEmpty())
                return setOf(PlayAllTreasuresPlayerAction())

        }

        //giving the engine help - don't buy if all treasures haven't been played
        if (playerState.currentBuys > 0 && (playerState.currentMoney >= cheapestAvailableCardToBuy(marketState))){

            val buyCardActions =  marketState.cardStacks
                .filter { it.countOfCard > 0 && it.card.costToBuy <= playerState.currentMoney }
                .map { cardInMarket -> BuyCardPlayerAction(cardInMarket.card) }
                if (buyCardActions.isEmpty()){
                    return setOf(SkipBuysAndEndTurnPlayerAction())
                }
                return buyCardActions/*.plus(SkipBuysAndEndTurnPlayerAction())*/.toSet() //TODO telling bot to never skip buys is cheatyface

        }

        return setOf(PassTurnPlayerAction())
    }

    private fun cheapestAvailableCardToBuy(marketState : MarketState) : Int {
        return marketState.cardStacks
            .map { it.card.costToBuy }
            .min() ?: throw RuntimeException("should always be at least 1 card stack")
    }
