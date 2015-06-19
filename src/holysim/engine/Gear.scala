package holysim.engine

import scala.collection.mutable

class Gear {
	val items = mutable.Set[Item]()
	val stats = mutable.Map[Stat, Int]().withDefaultValue(0)

	def apply(stat: Stat) = stats(stat)

	def equip(item: Item) = {
		items.add(item)
		item.stats.foreach(sv => stats(sv.stat) += sv.value)
	}
}

object Item {
	def apply(stats: StatValue*) = new Item(stats.toList)
	def apply(stats: List[StatValue]) = new Item(stats)
}

class Item(val stats: List[StatValue]) {
	def addStats(s: Seq[StatValue]) = Item(s ++: stats)
	def bonus(s: StatValue*) = addStats(s)
	def gem(s: StatValue*) = addStats(s)
	def enchant(s: StatValue*) = addStats(s)
}
