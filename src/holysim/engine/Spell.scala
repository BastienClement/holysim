package holysim.engine

import holysim.utils.Reactive

object Spell {
	trait Cooldown extends Spell {
		val cooldown: Reactive[Int]
	}
}

class Spell(val name: String) {

}
