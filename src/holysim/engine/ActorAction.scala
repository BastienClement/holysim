package holysim.engine

/**
 * An actor priority list
 */
case class ActorPriorityList(actions: ActorAction*) {
	/**
	 * Return the first available action in the list
	 */
	def select = actions.find(_.available)
}

/**
 * General class for an actor action
 */
trait ActorAction extends Action {
	/**
	 * The action owner
	 */
	val owner: Actor

	/**
	 * Define if this action is available at the moment
	 */
	def available: Boolean

	/**
	 * If true, a call to Actor.select() while executing ("casting") this action will cause it
	 * to be cancelled and replaced by the other action
	 */
	def interruptible: Boolean

	/**
	 * Begin action execution, return the resulting scheduled action
	 */
	def begin() : ScheduledAction[ActorAction]

	/**
	 * Execute the action
	 */
	def execute_action(): Unit

	/**
	 * Generic action execution
	 * - Ensure the action's owner is still performing this action
	 * - Call the abstract execute_action() method
	 * - Remove the action from owner's current_action field
	 * - Trigger selection of a new action to perform
	 */
	final def execute() = if (owner.current_action.action == this) {
		execute_action()
		owner.current_action = null
		owner.select()
	}
}

/**
 * Predefined ActorAction classes
 */
object ActorAction {
	/**
	 * Spell casting action with target selection DSL
	 */
	case class Cast(spell: Spell)(implicit val owner: Actor) extends ActorAction {
		/**
		 * The query selecting the spell target
		 * Default to cast on self
		 */
		private var target_query: QueryableActor = owner

		/**
		 * Requirement for this action, receive the actor selected by the query.
		 * A predicate failure at this stage doesn't trigger a new actor selection, proper actor
		 * filtering should be done at the query level.
		 */
		private var requirement: (Actor) => Boolean = (a) => true

		/**
		 * Reference to the current simulator
		 */
		private val sim = owner.sim

		/**
		 * Define the target query
		 */
		def on(t: QueryableActor) = {
			target_query = t
			this
		}

		/**
		 * Define the final requirement for this cast without actor consideration
		 */
		def when(req: => Boolean) = {
			requirement = _ => req
			this
		}

		/**
		 * Define the final requirement for this cast with selected actor consideration
		 */
		def when(req: (Actor) => Boolean) = {
			requirement = req
			this
		}

		/**
		 * Perform fresh target selection and then check requirements to determine availability of the action
		 */
		def available = target_query.refresh match {
			case Some(target) => requirement(target) && spell.available(target)
			case None => false
		}

		/**
		 * Spell casting are not interruptible
		 */
		var interruptible = false

		/**
		 * Begin spell casting, generate the SpellCastStart event and schedule a target-locked spell casting action
		 */
		def begin() = {
			// Lock the spell target
			val target = target_query.get.get

			// SpellCastStart event
			sim.trigger(SpellCastStartEvent(spell, owner, target, spell.effective_cast_time, spell.effective_gcd))

			// If cast time is shorter than GCD, add an additionnal callback to call Actor.select()
			// as soon as possible
			if (spell.effective_cast_time < spell.effective_gcd) {
				sim.schedule(spell.effective_gcd, () => owner.select())
			}

			// Schedule the Spell.cast call
			sim.schedule(spell.effective_cast_time, new ActorAction {
				val owner = Cast.this.owner
				val available = true
				val interruptible = false
				def begin() = ???
				def execute_action() = spell.cast(target)
			})
		}

		/**
		 * This action should never be scheduled directly, and thus never be executed
		 */
		def execute_action() = ???
	}

	/**
	 * Waiting action
	 * Does nothing when executed except triggering a new action selection
	 */
	case class Wait(time: Int)(implicit val owner: Actor) extends ActorAction {
		def available = true
		val interruptible = true
		def begin() = owner.sim.schedule(time, this)
		def execute_action() = {}
	}
}
