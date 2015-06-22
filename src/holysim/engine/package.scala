package holysim

import scala.language.implicitConversions

package object engine {
	implicit def doubleToInt(d: Double): Int = d.toInt
	implicit def longToInt(l: Long): Int = l.toInt

	trait WithIdentity {
		val identity: Symbol
	}

	object BoundSymbol {
		//implicit def fromAura[A <: Aura](aura: A): BoundSymbol[A] = aura.identity.bindTo[A]
		//implicit def fromSpell[S <: Spell](spell: S): BoundSymbol[S] = spell.identity.bindTo[S]
		implicit def fromAny[T <: WithIdentity](obj: T): BoundSymbol[T] = obj.identity.bindTo[T]
		implicit def fromSymbol[T](identity: Symbol): BoundSymbol[T] = identity.bindTo[T]
	}

	case class BoundSymbol[+T](sym: Symbol) extends AnyVal

	implicit class SymbolUtils(val sym: Symbol) extends AnyVal {
		def bindTo[T]: BoundSymbol[T] = BoundSymbol(sym)
		def +(str: String): Symbol = Symbol(sym.name + str)
	}
}
