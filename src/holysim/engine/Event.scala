package holysim.engine

trait Event {
	val sim = Simulator.current.value
	val time = sim.time
}

case class AuraGainedEvent(aura: Aura, source: Actor, target: Actor) extends Event
case class AuraLostEvent(aura: Aura, source: Actor, target: Actor) extends Event

trait SpellEvent extends Event {
	val spell: Spell
	val source: Actor
	val target: Actor
}

case class SpellCastEvent(spell: Spell, source: Actor, target: Actor) extends SpellEvent
case class SpellHealingEvent(spell: Spell, source: Actor, target: Actor, amount: Int, crit: Boolean, multistrike: Boolean) extends SpellEvent
