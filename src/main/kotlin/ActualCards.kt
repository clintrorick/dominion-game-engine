/****************/
/*Treasure cards*/
/****************/


class Gold : Card {
    override val cardName = GOLD_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 3
    override val providesBuys = 0
    override val providesCards = 0
    override val hasVPs = 0
    override val costToBuy = 6
    override val hasCardTypes = setOf(CardType.Treasure)
    override fun toString() : String {
        return cardName
    }
}

class Silver : Card {
    override val cardName = SILVER_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 2
    override val providesBuys = 0
    override val providesCards = 0
    override val hasVPs = 0
    override val costToBuy = 3
    override val hasCardTypes = setOf(CardType.Treasure)
    override fun toString() : String {
        return cardName
    }
}

class Copper : Card {
    override val cardName = COPPER_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 1
    override val providesBuys = 0
    override val providesCards = 0
    override val hasVPs = 0
    override val costToBuy = 0
    override val hasCardTypes = setOf(CardType.Treasure)
    override fun toString() : String {
        return cardName
    }
}

/***************/
/*Victory cards*/
/***************/

class Estate : Card {
    override val cardName = ESTATE_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 0
    override val providesBuys = 0
    override val providesCards = 0
    override val hasVPs = 1
    override val costToBuy = 2
    override val hasCardTypes = setOf(CardType.Victory)
    override fun toString() : String {
        return cardName
    }

}

class Duchy : Card {
    override val cardName = DUCHY_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 0
    override val providesBuys = 0
    override val providesCards = 0
    override val hasVPs = 3
    override val costToBuy = 5
    override val hasCardTypes = setOf(CardType.Victory)
    override fun toString() : String {
        return cardName
    }
}

class Province : Card {
    override val cardName = PROVINCE_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 0
    override val providesBuys = 0
    override val providesCards = 0
    override val hasVPs = 6
    override val costToBuy = 8
    override val hasCardTypes = setOf(CardType.Victory)
    override fun toString() : String {
        return cardName
    }
}

/***************/
/*Action cards*/
/***************/

class Village : Card {
    override val cardName = VILLAGE_CARD_NAME
    override val providesActions = 2
    override val providesMoney = 0
    override val providesBuys = 0
    override val providesCards = 1
    override val hasVPs = 0
    override val costToBuy = 3
    override val hasCardTypes = setOf(CardType.Action)
    override fun toString() : String {
        return cardName
    }
}

class Smithy : Card {
    override val cardName = SMITHY_CARD_NAME
    override val providesActions = 0
    override val providesMoney = 0
    override val providesBuys = 0
    override val providesCards = 3
    override val hasVPs = 0
    override val costToBuy = 4
    override val hasCardTypes = setOf(CardType.Action)
    override fun toString() : String {
        return cardName
    }
}

class Market : Card {
    override val cardName = MARKET_CARD_NAME
    override val providesActions = 1
    override val providesMoney = 1
    override val providesBuys = 1
    override val providesCards = 1
    override val hasVPs = 0
    override val costToBuy = 5
    override val hasCardTypes = setOf(CardType.Action)
    override fun toString() : String {
        return cardName
    }
}
