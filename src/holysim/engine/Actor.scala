package holysim.engine

trait Actor extends Aura.Target with Modifier.Target {
	implicit val impl_self = this
	implicit val sim = Simulator.current.value

	// Add self to simulator list of actors
	sim.actors.add(this)

	// Actor gear
	val gear = new Gear
	def equip(item: Item) = gear.equip(item)

	// Attempt to trigger procs
	def trigger(e: Event): Unit = {

	}
}
