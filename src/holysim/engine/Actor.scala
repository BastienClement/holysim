package holysim.engine

trait Actor extends Aura.Target {
	implicit val self = this
	implicit val sim: Simulator
}
