import kotlinx.coroutines.sync.Mutex
import org.clintrorick.*
import java.lang.Double.isNaN
import java.lang.Integer.max
import kotlin.math.ln
import kotlin.math.log

val mutex = Mutex()
fun updateStateActionReturnsBellman(statesAndActionsTraversedByP1ThisEpisode : MutableList<Pair<String,PlayerAction>>, endGameReward : Double){

    statesAndActionsTraversedByP1ThisEpisode.reverse()

    var qsaPlus1 = 0F.toDouble() //terminal state to start with
    var rewardTplus1 = endGameReward

    statesAndActionsTraversedByP1ThisEpisode.forEach {

        //it.first = base64 encoded gamestate, it.second = playeraction to string

        val thisSerializedStateAction = it.first + it.second.toString()

        val oldQSA : Double = qTable.getQValueForStateAction(thisSerializedStateAction) //default return to 0 if not yet visited
        //TODO is VST really Qsa? think so because it gives a return for a combination of state and action
        val numTimesThisActionPulled = qTable.getNumPullsOverAllTrainingForStateAction( thisSerializedStateAction )
        //because the numerator is static, the learning rate will decay over time over all action states
        //TODO move this algo to GameDriver

        var dynamic_learning_rate : Double = dynamicLearningRate(numTimesThisActionPulled)

        if (dynamic_learning_rate > 0.5F)
            dynamic_learning_rate = 0.5F.toDouble()

        if (dynamic_learning_rate < 0.10F)
            dynamic_learning_rate = 0.10F.toDouble()

        val newQSA : Double = bellmanBackup(oldQSA = oldQSA,
                                            dynamic_learning_rate = dynamic_learning_rate,
                                            rewardTplus1 = rewardTplus1,
                                            qsaPlus1 = qsaPlus1)

        rewardTplus1 = 0F.toDouble() // only the terminal state gives a reward, value is what is backpropagated


        val updatedNumTimesPulled = numTimesThisActionPulled + 1

//        runBlocking {
            /* mutex.withLock {*/  qTable.getQTable()[ thisSerializedStateAction ] = QTableEntry( newQSA , updatedNumTimesPulled ) //}
//        }

        if (isNaN(newQSA.toDouble())){
            println("here")
        }
        qsaPlus1 = newQSA

    }
    statesAndActionsTraversedByP1ThisEpisode.reverse()

    //V(St) <- V(St) + alpha [Rt+1 + gamma V(St+1) - V(St)]
    //for first SA visited while going backwards, Rt+1 is either 1 or -1
    //for second to nth SA visited while going backwards, Rt+1 is 0
    //for second to nth SA visited while going backwards, V(St+1) is what we just calc'd

}
