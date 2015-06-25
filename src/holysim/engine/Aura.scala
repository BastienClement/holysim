package holysim.engine

import scala.collection.mutable
import scala.language.implicitConversions
import holysim.utils.{CallbackList, CallbackListArg, HigherKindedMemoized, Reactive}

class Aura(val identity: Symbol)(implicit impl_owner: Actor) extends WithIdentity {
	type self = this.type
	val owner = impl_owner
	val sim = owner.sim

	val onGain = EventHook[AuraGainedEvent]()
	val onLose = EventHook[AuraLostEvent]()
	val onRefresh = new CallbackListArg[self]()

	override def toString = identity.name
}

object Aura {
	/**
	 * Provide Aura management method on actors
	 */
	trait Target {
		this: Actor =>

		/** Cache of actor's auras */
		type ActorMapTo[T] = mutable.Map[Actor, T]
		val auras = new HigherKindedMemoized[Aura, BoundSymbol, ActorMapTo] {
			def default[A <: Aura](key: BoundSymbol[A]) = mutable.Map[Actor, A]()
		}

		/** Apply a new aura to this actor */
		def gain[A <: Aura](aura: A)(implicit owner: Actor) = {
			val instances = auras.get(aura.identity.bindTo[A])
			instances.get(owner) match {
				case Some(instance) =>
					instance.onRefresh(aura.asInstanceOf[instance.type])
				case None =>
					instances.put(owner, aura)
					aura.onGain(AuraGainedEvent(aura, owner, this))
			}
		}

		/**
		 * Removes an aura
		 */
		def get[A <: Aura](aura_symbol: BoundSymbol[A])(implicit owner: Actor): Option[A] = auras.get(aura_symbol).get(owner)

		/**
		 * Check the presence of an aura
		 */
		def has[A <: Aura](aura_symbol: BoundSymbol[A])(implicit owner: Actor): Boolean = auras.get(aura_symbol).get(owner).isDefined

		/**
		 * Removes an aura
		 */
		def lose[A <: Aura](aura_symbol: BoundSymbol[A])(implicit owner: Actor): Unit = {
			val instances = auras.get(aura_symbol)
			instances.get(owner) match {
				case Some(instance) =>
					instances.remove(owner)
					instance.onLose(AuraLostEvent(instance, owner, this))
				case None =>
				/* Nothing */
			}
		}
	}

	/**
	 * Keep track of aura target
	 */
	trait WithTarget extends Aura {
		var target: Actor = null

		onGain += { ev =>
			assert(target == null)
			target = ev.target
		}

		onLose += { ev =>
			assert(ev.target == target)
			target = null
		}
	}

	/**
	 * Add stats modifier to an Aura
	 */
	trait Modifiers extends Aura {
		val modifiers = mutable.ArrayBuffer[ModifierValue[_]]()

		onGain += { ev =>
			modifiers foreach { mv =>
				ev.target.modifiers_effects(mv.mod).add(mv.value)
				mv.value ~> ev.target.modifier(mv.mod)
			}
		}

		onLose += { ev =>
			modifiers foreach { mv =>
				ev.target.modifiers_effects(mv.mod).remove(mv.value)
				mv.value ~/> ev.target.modifier(mv.mod)
			}
		}
	}

	/**
	 * Add Duration handling to an Aura
	 */
	trait Duration extends Aura with WithTarget {
		// Make aura's owner implicit in this trait
		private[this] implicit val impl_owner = owner

		val duration: Int

		private[this] var expire_action: ScheduledAction = null
		private[this] def schedule_expiration(delay: Int) = {
			if (expire_action != null) expire_action.cancel()
			expire_action = sim.after(delay + 1) {
				target lose this
			}
		}

		onGain += (_ => schedule_expiration(duration))
		onRefresh += (other => schedule_expiration(other.duration))
	}

	/**
	 * Add stack management to aura
	 */
	trait Stackable extends Aura {
		val stacks = Reactive {0}
		val max_stacks: Int

		onGain += (_ => stacks := 1)
		onLose += (_ => stacks := 0)

		onRefresh += (other => if (stacks < max_stacks) stacks ~ (_ + other.stacks))
	}

	/**
	 * Single target aura
	 */
	trait SingleTarget extends Aura with WithTarget {
		// Make aura's owner implicit in this trait
		private[this] implicit val impl_owner = owner

		/** Remove the aura from the previous target once applied to someone else */
		onGain ~= (_ => if (target != null) target lose this)
	}

	/**
	 * Ticking aura
	 */
	trait Ticking extends Aura with WithTarget {
		val interval: Int
		lazy val effective_interval = Reactive(interval)

		val onTick = new CallbackList()
		def tick() = onTick()

		private[this] var next_tick: ScheduledAction = null
		private[this] def schedule_next(): Unit = next_tick = sim.schedule(effective_interval, () => tick())

		onGain += (_ => schedule_next())
		onTick += schedule_next()
		onLose += (_ => next_tick.cancel())
	}

	/**
	 * An ticking aura with the first tick happening instantly
	 */
	trait TicksInstantly extends Ticking {
		onGain += (_ => tick())
	}

	/**
	 * An aura with ticks interval affected by haste
	 */
	trait HastedTicks extends Ticking {
		//override lazy val effective_interval = Reactive[Int](interval / owner.haste)
	}

	/**
	 * A DoT/HoT
	 */
	trait PeriodicAura extends HastedTicks with Duration {
		val healing: Reactive[Int]

		val spell = new Tick
		class Tick private[PeriodicAura] extends Spell(identity)(owner) {

		}

		onTick += spell.cast(target)
	}
}
