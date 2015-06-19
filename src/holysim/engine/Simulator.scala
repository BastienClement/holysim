package holysim.engine

object Simulator {
	def apply(init: (Simulator) => Unit) = {
		val sim = new Simulator
		init(sim)
		sim
	}
}

class Simulator {
	var time: Int = 0

	def run() = {

	}
}
