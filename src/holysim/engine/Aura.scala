package holysim.engine

import holysim.utils.{Memoized, Reactive}

import scala.collection.mutable
import scala.reflect._

class Aura(name: String)(implicit o: Actor) {
	type self = this.type
	val owner = o

	def on_gain(target: Aura.Target) = {}
	def on_lose(target: Aura.Target) = {}
	def on_refresh(other: self) = {}
}

object Aura {
	/**
	 * Provide Aura management method on actors
	 */
	trait Target {
		this: Actor =>
		private val auras = Memoized { (mod: Class[_]) => mutable.Map[Actor, Aura]() }

		def gain(aura: Aura)(implicit owner: Actor) = {
			val instances = auras(aura.getClass)
			instances.get(owner) match {
				case Some(instance) =>
					instance.on_refresh(aura.asInstanceOf[instance.type])
				case None =>
					instances(owner) = aura
					aura.on_gain(this)
			}
		}

		def has(clazz: Class[_])(implicit owner: Actor): Boolean = auras(clazz).get(owner).isDefined

		def has[A <: Aura](implicit owner: Actor, ct: ClassTag[A]): Boolean = has(ct.runtimeClass)(owner)
		def has[A <: Aura](aura: A)(implicit owner: Actor, ct: ClassTag[A]): Boolean = has(owner, ct)

		def lose(clazz: Class[_])(implicit owner: Actor): Unit = {
			val instances = auras(clazz)
			instances.get(owner) match {
				case Some(instance) =>
					instance.on_lose(this)
					instances.remove(owner)
				case None =>
				/* Nothing */
			}
		}

		def lose[A <: Aura](implicit owner: Actor, ct: ClassTag[A]): Unit = lose(ct.runtimeClass)(owner)
		def lose[A <: Aura](aura: A)(implicit owner: Actor, ct: ClassTag[A]): Unit = lose(owner, ct)

		private[Aura] val aura_effects = Memoized { (mod: Modifier[_]) => mutable.Set[Reactive[_]]() }

		private def build_modifier[T](mod: Modifier[T]) = {
			Reactive {
				val effects = aura_effects(mod).asInstanceOf[Traversable[Reactive[T]]]
				effects.map(_.value).fold(mod.base)(mod.fold)
			}
		}

		private val modifiers = Memoized((mod: Modifier[_]) => build_modifier(mod))
		def modifier[T](mod: Modifier[T]): Reactive[T] = modifiers(mod).asInstanceOf[Reactive[T]]
	}

	/**
	 * Add stats modifier to an Aura
	 */
	trait Modifiers extends Aura {
		val modifiers: Traversable[ModifierValue[_]]

		abstract override def on_gain(target: Target) = {
			super.on_gain(target)
			modifiers foreach { mv =>
				target.aura_effects(mv.mod).add(mv.value)
				mv.value ~> target.modifier(mv.mod)
			}
		}

		abstract override def on_lose(target: Target) = {
			super.on_lose(target)
			modifiers foreach { mv =>
				target.aura_effects(mv.mod).remove(mv.value)
				mv.value ~/> target.modifier(mv.mod)
			}
		}
	}

	/**
	 * Add Duration handling to an Aura
	 */
	trait Duration extends Aura {
		val duration: Int

		abstract override def on_gain(target: Target) = {
			super.on_gain(target)
		}

		abstract override def on_lose(target: Target) = {
			super.on_lose(target)
		}

		abstract override def on_refresh(other: self) = {
			super.on_refresh(other)
			println("extend duration", other.duration)
		}
	}

	/**
	 * Add stack management to aura
	 */
	trait Stackable extends Aura {
		val stacks = Reactive {0}
		val max_stacks: Int

		abstract override def on_gain(target: Target) = {
			super.on_gain(target)
			stacks := 1
		}

		abstract override def on_lose(target: Target) = {
			super.on_lose(target)
			stacks := 0
		}

		abstract override def on_refresh(other: self) = {
			super.on_refresh(other)
			if (stacks < max_stacks) {
				stacks ~ (_ + other.stacks)
			}
		}
	}
}
