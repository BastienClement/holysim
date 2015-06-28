package holysim.engine

import scala.language.implicitConversions
import holysim.engine.ActorQuery._
import holysim.engine.ActorQueryInternal._
import holysim.utils.Reactive

/**
 * Interface that abstract away the use of a query
 */
trait QueryableActor {
	def get: Option[Actor]
	def flush(): Unit

	/**
	 * Shortcut for flush() then get()
	 */
	def refresh = {
		flush()
		get
	}
}

object QueryableActor {
	implicit def fromActor(a: Actor): QueryableActor = StaticActorQuery(a)
	implicit def toActor(qa: QueryableActor): Option[Actor] = qa.get
	implicit def toStrictActor(qa: QueryableActor): Actor = qa.get.get
}

/**
 * A dummy query that never match anything
 */
case object DummyActorQuery extends QueryableActor {
	def get = None
	def flush() = {}
}

/**
 * A query that always match the same actor
 */
case class StaticActorQuery(actor: Actor) extends QueryableActor {
	val get = Option(actor)
	def flush() = {}
}

/**
 * Actor query object
 */
case class ActorQuery(sim: Simulator) extends QueryableActor {
	/** Static filters are evaluated only once */
	private var staticFilters = Vector[(Actor) => Boolean]()

	/** Dynamic filters are evaluated every time */
	private var dynamicFilters = Vector[(Actor) => Boolean]()

	/** Use to favorise some targets */
	private var preferPartition: (Actor) => Boolean = null

	/** Every actors from the simulation */
	private lazy val full_pool = sim.actors_pool

	/** Actors matching static filters */
	private lazy val base_pool = if (staticFilters.nonEmpty) full_pool.filter(a => staticFilters.forall(_(a))) else full_pool

	/** Actors matching both static and dynamic filter */
	private val pool = Reactive {
		if (dynamicFilters.nonEmpty) base_pool.filter(a => dynamicFilters.forall(_(a)))
		else base_pool
	}

	/** The selector strategy, defaults to random */
	private var selector: ActorSelector = RandomSelector

	/** The next Query if this one doesn't match anything */
	private var next: QueryableActor = DummyActorQuery

	/** The selected actor cache */
	private val selected = Reactive {
		// Apply preference
		val final_pool = if (preferPartition != null) {
			val (first, second) = pool.partition(preferPartition)
			if (first.nonEmpty) first else second
		} else pool.value

		// Apply selector and next query
		selector.select(final_pool) orElse next.get
	}

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

	def get: Option[Actor] = selected.value
	def count = pool.size

	def flush() = {
		selected.invalidate()
		next.flush()
	}
}

object ActorQueryInternal {
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
		def select(pool: IndexedSeq[Actor]): Option[Actor]
	}

	abstract class QueryBoostrap
	object QueryBoostrap {
		implicit def bootstrap(s: QueryBoostrap)(implicit sim: Simulator): ActorQuery = ActorQuery(sim)
	}
}

object ActorQuery {
	/** Dummy object to bootstrap actor query */
	object query extends QueryBoostrap

	// Implicitly add Static or Dynamic tags to simple predicate functions
	implicit def predicateToStatic(predicate: (Actor) => Boolean): StaticFilter = new ActorCustomFilter(predicate) with StaticFilter
	implicit def predicateToDynamic(predicate: (Actor) => Boolean): DynamicFilter = new ActorCustomFilter(predicate) with DynamicFilter

	// Actor category filters  ==========================================================================================

	object Unit extends ActorDummyFilter

	object Hostile extends ActorFilter with StaticFilter { def apply(actor: Actor) = actor.reaction == Actor.Reaction.Hostile }
	object Friendly extends ActorFilter with StaticFilter { def apply(actor: Actor) = actor.reaction == Actor.Reaction.Friendly }

	object Tank extends ActorFilter with StaticFilter { def apply(actor: Actor) = actor.role == Actor.Role.Tank }
	object Healer extends ActorFilter with StaticFilter { def apply(actor: Actor) = actor.role == Actor.Role.Healer }
	object Damager extends ActorFilter with StaticFilter { def apply(actor: Actor) = actor.role == Actor.Role.Damage }

	// Actor selectors ==================================================================================================

	/** Select a random actor from the pool */
	object RandomSelector extends ActorSelector {
		def select(pool: IndexedSeq[Actor]) = pool.size match {
			case 0 => None
			case 1 => Option(pool.head)
			case size => Option(pool(Simulator.current.rng.nextInt(size)))
		}
	}

	/**
	 * Selects the most injured actor from the pool
	 * Full health actor are not matched
	 */
	object MostInjuredSelector extends ActorSelector {
		def select(pool: IndexedSeq[Actor]) = {
			var max_absolute = 1
			var max_percent = 0.0
			var candidates: List[Actor] = Nil

			for (actor <- pool) {
				val actor_absolute = 0
				val actor_percent = 0.0

				if (actor_percent > max_percent || (actor_percent == max_percent && actor_absolute > max_absolute)) {
					candidates = actor :: Nil
					max_absolute = actor_absolute
					max_percent = actor_percent
				} else if (actor_percent == max_percent && actor_absolute == max_absolute) {
					candidates = actor :: candidates
				}
			}

			if (candidates.size > 1)
				RandomSelector.select(candidates.toIndexedSeq)
			else
				candidates.headOption
		}
	}

	object InjuredSelector extends ActorSelector {
		def select(pool: IndexedSeq[Actor]) = ???
	}

	case class NthSelector(i: Int) extends ActorSelector {
		assert(i > 0)
		val index = i - 1

		def select(pool: IndexedSeq[Actor]) = pool.size match {
			case size if size > index => Option(pool(index))
			case _ => None
		}
	}
}
