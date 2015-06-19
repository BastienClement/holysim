package holysim.paladin

import holysim.engine.{Actor, Simulator}

class Paladin(implicit val sim: Simulator) extends Actor with PaladinAuras with PaladinProcs with PaladinSpells {
	println("Creating a paladin")
}
