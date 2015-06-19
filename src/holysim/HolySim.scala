package holysim

import holysim.engine._
import holysim.paladin._

object HolySim extends App {
	val sim = Simulator { implicit s =>
		implicit val p = new Paladin
	}

	println(sim)
}
