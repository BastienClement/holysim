package holysim.engine

import scala.language.implicitConversions
import scala.util.Random
import holysim.engine.ActorQuery._
import holysim.engine.ActorQueryInternal._

/**
 * Interface that abstract away the use of a query
 */
trait QueryableActor {
	def get: Option[Actor]
}

object QueryableActor {
	implicit def fromActor(a: Actor): QueryableActor = StaticActorQuery(a)
}

/**
 * A dummy query that never match anything
 */
case object DummyActorQuery extends QueryableActor {
	def get = None
}

/**
 * A query that always match the same actor
 */
case class StaticActorQuery(actor: Actor) extends QueryableActor {
	val get = Option(actor)
}

/**
 * Actor query object
 */
case class ActorQuery(sim: Simulator) extends QueryableActor {
	/** Static filters are evaluated only once */
	private[this] var staticFilters = Vector[(Actor) => Boolean]()

	/** Dynamic filters are evaluated every time */
	private[this] var dynamicFilters = Vector[(Actor) => Boolean]()

	/** Use to favorise some targets */
	private[this] var preferPartition: (Actor) => Boolean = null

	/** Every actors from the simulation */
	private[this] lazy val full_pool = sim.actors_pool

	/** Actors matching static filters */
	private[this] lazy val base_pool = if (staticFilters.nonEmpty) full_pool.filter(a => staticFilters.forall(_(a))) else full_pool

	/** Actors matching both static and dynamic filter */
	private[this] def pool = if (dynamicFilters.nonEmpty) base_pool.filter(a => dynamicFilters.forall(_(a))) else base_pool

	/** The selector strategy, defaults to random */
	private[this] var selector: ActorSelector = RandomSelector

	/** The selected actor cache */
	private[this] var selected: Option[Actor] = null
	private[this] var selected_control: Long = -1

	/** The next Query if this one doesn't match anything */
	private[this] var next: QueryableActor = DummyActorQuery

	def accordingTo(s: ActorSelector) = {
		selector = s
		this
	}

	def from(filter: StaticFilter) = {
		if (!filter.dummy) staticFilters :+= filter
		this
	}

	def injured(what: StaticFilter) = this accordingTo InjuredSelector from what
	def mostInjured(what: StaticFilter) = this accordingTo MostInjuredSelector from what
	def random(what: StaticFilter) = this accordingTo RandomSelector from what
	def nth(n: Int) = this accordingTo NthSelector(n)

	def first(what: StaticFilter) = this nth 1 from what
	def second(what: StaticFilter) = this nth 2 from what
	def third(what: StaticFilter) = this nth 3 from what

	def when(filter: DynamicFilter) = {
		if (!filter.dummy) dynamicFilters :+= filter
		this
	}

	def prefer(partition: DynamicFilter) = {
		if (!partition.dummy) preferPartition = partition
		this
	}

	def excluding(filter: DynamicFilter) = {
		dynamicFilters :+= ((a: Actor) => !filter(a))
		this
	}

	def or(n: QueryableActor) = {
		next = n
		this
	}

	/** Perform actor selection */
	def get: Option[Actor] = {
		if (selected != null && selected_control == ActorQueryInternal.cache_control) {
			selected
		} else {
			// Apply preference
			val final_pool = if (preferPartition != null) {
				val (first, second) = pool.partition(preferPartition)
				if (first.nonEmpty) first else second
			} else pool

			// Apply selector and next query
			selected = selector.select(final_pool) orElse next.get
			selected_control = ActorQueryInternal.cache_control
			selected
		}
	}
}

object ActorQueryInternal {
	/** When this value changes, all matches caches are flushed */
	var cache_control: Long = 0

	/** Generic interface of a filter function container */
	trait ActorFilter extends (Actor => Boolean) {
		val dummy: Boolean = false
	}

	trait StaticFilter extends ActorFilter
	trait DynamicFilter extends ActorFilter

	trait ActorDummyFilter extends ActorFilter with StaticFilter with DynamicFilter {
		override val dummy = true
		def apply(a: Actor) = ???
	}

	class ActorCustomFilter(predicate: (Actor) => Boolean) extends ActorFilter {
		def apply(a: Actor) = predicate(a)
	}

	/** Generic interface of an actor selector */
	trait ActorSelector {
		def select(pool: Vector[Actor]): Option[Actor]
	}
}

object ActorQuery {
	/** Dummy object to bootstrap actor query */
	object select
	implicit def selectBootstrap(s: select.type)(implicit sim: Simulator): ActorQuery = ActorQuery(sim)

	/** Flush the matching cache */
	def flush() = ActorQueryInternal.cache_control += 1

	implicit def predicateToStatic(predicate: (Actor) => Boolean): StaticFilter = new ActorCustomFilter(predicate) with StaticFilter
	implicit def predicateToDynamic(predicate: (Actor) => Boolean): DynamicFilter = new ActorCustomFilter(predicate) with DynamicFilter

	// Actor category filters  ==========================================================================================
	object Actor extends ActorDummyFilter
	object Enemy extends ActorFilter with StaticFilter { def apply(actor: Actor) = true }
	object Player extends ActorFilter with StaticFilter { def apply(actor: Actor) = true }
	object Tank extends ActorFilter with StaticFilter { def apply(actor: Actor) = true }
	object Healer extends ActorFilter with StaticFilter { def apply(actor: Actor) = true }
	object Damager extends ActorFilter with StaticFilter { def apply(actor: Actor) = true }

	// Actor selectors ==================================================================================================
	object RandomSelector extends ActorSelector {
		def select(pool: Vector[Actor]) = pool.size match {
			case 0 => None
			case 1 => Option(pool.head)
			case size => Option(pool(Random.nextInt(size)))
		}
	}

	object MostInjuredSelector extends ActorSelector {
		def select(pool: Vector[Actor]) = {
			var maxHealthDeficit: Int = 1
			var candidates = Vector[Actor]()

			for (candidate <- pool) {
				0 match {
					case deficit if deficit > maxHealthDeficit =>
						// More injured than any previously encountered actor
						maxHealthDeficit = deficit
						candidates = Vector(candidate)
					case deficit if deficit == maxHealthDeficit =>
						// As much injured than the most injured until now
						candidates :+= candidate
					case _ =>
					// No so much injured
				}
			}

			RandomSelector.select(candidates)
		}
	}

	object InjuredSelector extends ActorSelector {
		def select(pool: Vector[Actor]) = pool(Random.nextInt(pool.size))
	}

	case class NthSelector(i: Int) extends ActorSelector {
		assert(i > 0)
		val index = i - 1

		def select(pool: Vector[Actor]) = pool.size match {
			case size if size > index => Option(pool(index))
			case _ => None
		}
	}
}
