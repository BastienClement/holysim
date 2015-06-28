package holysim.engine

import scala.collection.mutable
import scala.language.implicitConversions
import holysim.utils.{CallbackList, CallbackListArg, HigherKindedMemoized, Reactive}

/**
 * An aura is a buff or debuff that can be applied to actors
 */
class Aura(val identity: Symbol)(implicit impl_owner: Actor) extends WithIdentity {
	/**
	 * Self type, used as a type argument for the onRefresh event
	 */
	type self = this.type

	/**
	 * The aura owner is the actor that will cast / casted this aura
	 */
	val owner = impl_owner

	/**
	 * Reference to the current simulator
	 */
	val sim = owner.sim

	/**
	 * Aura life-cycle events
	 */
	val onGain = EventHook[AuraGainedEvent]()
	val onLose = EventHook[AuraLostEvent]()
	val onRefresh = EventHook[AuraRefreshEvent[self]]()

	/**
	 * The aura string representation is its identity symbol name
	 */
	override def toString = identity.name
}

/**
 * Predefined Aura-related trait
 */
object Aura {
	/**
	 * Provide Aura management method on actors
	 */
	trait Target {
		this: Actor =>

		/**
		 * Cache of actor's auras
		 */
		type ActorMapTo[T] = mutable.Map[Actor, T]
		val auras = new HigherKindedMemoized[Aura, BoundSymbol, ActorMapTo] {
			def default[A <: Aura](key: BoundSymbol[A]) = mutable.Map[Actor, A]()
		}

		/**
		 * Apply a new aura to this actor
		 */
		def gain[A <: Aura](aura: A) = {
			val instances = auras.get(aura.identity.bindTo[A])
			instances.get(aura.owner) match {
				case Some(instance) =>
					instance.onRefresh(AuraRefreshEvent(instance, instance.owner, this, aura.asInstanceOf[instance.type]))
				case None =>
					instances.put(aura.owner, aura)
					aura.onGain(AuraGainedEvent(aura, aura.owner, this))
			}
		}

		/**
		 * Removes an aura
		 */
		def get[A <: Aura](aura_symbol: BoundSymbol[A])(implicit owner: Actor): Option[A] = auras.get(aura_symbol).get(owner)

		/**
		 * Check the presence of an aura on this actor
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
	 * Mixing-in this trait will also prevent that the same aura instance
	 * to be applied to multiple targets simultaneously
	 */
	trait WithTarget extends Aura {
		/**
		 * The (current) aura target
		 */
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
		/**
		 * Modifiers applied by this aura
		 */
		var modifiers = mutable.ArrayBuffer[ModifierValue[_]]()

		/**
		 * Add modifiers to the target when gaining the aura
		 */
		private def applyModifiers(target: Actor) = modifiers foreach { mv =>
			target.modifiers_effects(mv.mod).add(mv.value)
			mv.value ~> target.modifier(mv.mod)
		}

		/**
		 * Remove modifiers from the target
		 */
		private def removeModifiers(target: Actor) = modifiers foreach { mv =>
			target.modifiers_effects(mv.mod).remove(mv.value)
			mv.value ~/> target.modifier(mv.mod)
		}

		onGain += (ev => applyModifiers(ev.target))
		onLose += (ev => removeModifiers(ev.target))

		onRefresh += { ev =>
			removeModifiers(ev.target)
			modifiers = ev.other.modifiers
			applyModifiers(ev.target)
		}
	}

	/**
	 * Add Duration handling to an Aura
	 */
	trait Duration extends Aura with WithTarget {
		// Make aura's owner implicit in this trait
		private implicit val impl_owner = owner

		/**
		 * Aura duration
		 */
		val duration: Int

		/**
		 * The expiration action
		 */
		private var expire_action: ScheduledAction[Action] = null

		/**
		 * Schedule the aura expiration
		 */
		private def schedule_expiration(delay: Int) = {
			if (expire_action != null) expire_action.cancel()
			expire_action = sim.after(delay + 1) {
				target lose this
			}
		}

		/**
		 * On gain, schedule the expiration following this aura duration field
		 */
		onGain += (_ => schedule_expiration(duration))

		/**
		 * Compute the duration after aura refresh
		 */
		def refresh_duration(other: Duration) = other.duration

		/**
		 * When refreshing an aura with duration, extends the current one based on the duration of the other instance
		 */
		onRefresh += (ev => schedule_expiration(refresh_duration(ev.other)))
	}

	/**
	 * Add stack management to aura
	 */
	trait Stackable extends Aura {
		/**
		 * The initial number of stacks
		 * Defaults to 1
		 */
		val inital_stacks = 1

		/**
		 * Current stack count
		 */
		val stacks = Reactive(inital_stacks)

		/**
		 * Maximum number of stacks allowed
		 */
		val max_stacks: Int

		/**
		 * Compute the resulting number of stack on refresh
		 */
		def refresh_stacks(other: Stackable) = math.min(max_stacks, stacks.value + other.stacks.value)
		onRefresh += (ev => stacks := refresh_stacks(ev.other))
	}

	/**
	 * Single target aura
	 * When a aura with this trait is applied to a new target, the previous target loses the aura
	 */
	trait SingleTarget extends Aura with WithTarget {
		// Make aura's owner implicit in this trait
		private implicit val impl_owner = owner

		/**
		 * Remove the aura from the previous target once applied to someone else
		 */
		onGain ~= (_ => if (target != null) target lose this)
	}

	/**
	 * Ticking aura
	 */
	trait Ticking extends Aura with WithTarget {
		/**
		 * Base ticks interval
		 */
		val interval: Int

		/**
		 * The actual ticks interval, allow overloading
		 */
		val effective_interval = Reactive(interval)

		/**
		 * The tick event hook
		 */
		val onTick = new CallbackList()
		def tick() = onTick()

		/**
		 * The next tick event
		 */
		private var next_tick: ScheduledAction[Action] = null

		/**
		 * Schedule the next tick
		 */
		private def schedule_next(): Unit = next_tick = sim.schedule(effective_interval, () => tick())

		/**
		 * On application, schedule the first tick
		 */
		onGain += (_ => schedule_next())

		/**
		 * On each tick, schedule the next one
		 */
		onTick += schedule_next()

		/**
		 * On fade, cancel an possible next tick
		 */
		onLose += (_ => next_tick.cancel())
	}

	/**
	 * An ticking aura with the first tick happening instantly
	 * Simply call the tick() method on aura application
	 */
	trait TicksInstantly extends Ticking {
		onGain += (_ => tick())
	}

	/**
	 * An aura with ticks interval affected by haste
	 */
	trait HastedTicks extends Ticking {
		override val effective_interval = Reactive[Int](interval / owner.haste)
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
