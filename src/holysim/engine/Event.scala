package holysim.engine

import holysim.utils.CallbackListArg

trait Event {
	val sim = Simulator.current
	val time = sim.time
}

case class SimulationBeginEvent() extends Event
case class SimulationEndEvent(state: Simulator.State.Value) extends Event

case class AuraGainedEvent(aura: Aura, source: Actor, target: Actor) extends Event
case class AuraRefreshEvent[T <: Aura](aura: Aura, source: Actor, target: Actor, other: T) extends Event
case class AuraLostEvent(aura: Aura, source: Actor, target: Actor) extends Event
case class AuraTickEvent(aura: Aura, source: Actor, target: Actor) extends Event

trait SpellEvent extends Event {
	val spell: Spell
	val source: Actor
	val target: Actor
}

case class SpellCastStartEvent(spell: Spell, source: Actor, target: Actor, cast_time: Int, gcd: Int) extends SpellEvent
case class SpellCastSuccessEvent(spell: Spell, source: Actor, target: Actor, resource: Option[Any], cost: Int) extends SpellEvent
case class SpellHealingEvent(spell: Spell, source: Actor, target: Actor, amount: Int, crit: Boolean, multistrike: Boolean) extends SpellEvent

case class EventHook[E <: Event]() extends CallbackListArg[E] {
	this += (e => e.sim.trigger(e))
}
