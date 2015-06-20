package holysim.engine

import holysim.utils.{CallbackListArg, Memoized, Reactive}

import scala.collection.mutable
import scala.reflect._

class Aura(name: String)(implicit impl_owner: Actor) {
	type self = this.type
	val owner = impl_owner

	val onGain = new CallbackListArg[Actor]()
	val onLose = new CallbackListArg[Actor]()
	val onRefresh = new CallbackListArg[self]()
}

object Aura {
	/**
	 * Provide Aura management method on actors
	 */
	trait Target {
		this: Actor =>

		private val auras = Memoized { (mod: Class[_]) => mutable.Map[Actor, Aura]() }

		/**
		 * Apply a new aura to this actor
		 */
		def gain(aura: Aura)(implicit owner: Actor) = {
			val instances = auras(aura.getClass)
			instances.get(owner) match {
				case Some(instance) =>
					instance.onRefresh(aura.asInstanceOf[instance.type])
				case None =>
					instances(owner) = aura
					aura.onGain(this)
			}
		}

		/**
		 * Check the presence of an aura
		 */
		def has(clazz: Class[_])(implicit owner: Actor): Boolean = auras(clazz).get(owner).isDefined
		def has[A <: Aura](implicit owner: Actor, ct: ClassTag[A]): Boolean = has(ct.runtimeClass)(owner)
		def has[A <: Aura](aura: A)(implicit owner: Actor, ct: ClassTag[A]): Boolean = has(owner, ct)

		/**
		 * Removes an aura
		 */
		def lose(clazz: Class[_])(implicit owner: Actor): Unit = {
			val instances = auras(clazz)
			instances.get(owner) match {
				case Some(instance) =>
					instance.onLose(this)
					instances.remove(owner)
				case None =>
				/* Nothing */
			}
		}
		def lose[A <: Aura](implicit owner: Actor, ct: ClassTag[A]): Unit = lose(ct.runtimeClass)(owner)
		def lose[A <: Aura](aura: A)(implicit owner: Actor, ct: ClassTag[A]): Unit = lose(owner, ct)
	}

	/**
	 * Add stats modifier to an Aura
	 */
	trait Modifiers extends Aura {
		val modifiers = mutable.ArrayBuffer[ModifierValue[_]]()

		onGain += { target =>
			modifiers foreach { mv =>
				target.modifiers_effects(mv.mod).add(mv.value)
				mv.value ~> target.modifier(mv.mod)
			}
		}

		onLose += { target =>
			modifiers foreach { mv =>
				target.modifiers_effects(mv.mod).remove(mv.value)
				mv.value ~/> target.modifier(mv.mod)
			}
		}
	}

	/**
	 * Add Duration handling to an Aura
	 */
	trait Duration extends Aura {
		val duration: Int
		onRefresh += { other => println("extend duration", other.duration) }
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
	trait SingleTarget {
		var target: Actor = null
	}
}
