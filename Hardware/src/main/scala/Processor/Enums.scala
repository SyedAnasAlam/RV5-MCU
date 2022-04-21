import chisel3._
import chisel3.experimental.ChiselEnum

object eAluOp extends ChiselEnum {
    val eARITHMETIC, eBRANCH, eLoadStoreJump, eLUIOP, eAUIPCOP = Value
}

object eAluControl extends ChiselEnum {
    val eADD, eSUB, eXOR, eOR, eAND, eSLL, eSRL , eSRA , eSLT , eSLTU , eEQU , eNEQ , eLT , eGE , eLTU , eGEU, eLUI, eAUIPC = Value

}

object eRegSrc extends ChiselEnum {
val eALU, eMEM, eJUMP = Value
}

object ePCSrc extends ChiselEnum {
    val ePC_4, ePC_JUMP, ePC_BRANCH, ePC_JALR = Value
}



