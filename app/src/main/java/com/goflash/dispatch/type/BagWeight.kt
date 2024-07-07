package com.goflash.dispatch.type

enum class BagWeight(val displayName: String,val weight: Double) {
    KG_10("10 KG", 10.0), KG_15("15 KG", 15.0), KG_25("25 KG", 25.0),
    KG_40("40 KG", 40.0);

    companion object {
        fun toStringList(): ArrayList<String> {
            val list = ArrayList<String>()
            for (mode in BagWeight.values())
                list.add(mode.displayName)
            return list
        }

        fun getWeightFromPosition(position: Int): Double{
            val list = BagWeight.values()
            val bagWeight = list[position]
            return bagWeight.weight
        }

        fun getPositionFromWeight(weight: Double): Int{
            val list = BagWeight.values()
            val bagWeight = list.find { it.weight == weight }
            return list.indexOf(bagWeight)
        }
    }

}