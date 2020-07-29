package com.chandsemma.greatkitchens.util

import com.chandsemma.greatkitchens.model.Restaurant

class Sorter {
    companion object {
        var costComparator = Comparator<Restaurant> { res1, res2 ->
            val costOne = res1.costForOne
            val costTwo = res2.costForOne
            if (costOne.compareTo(costTwo) == 0) {
                ratingComparator.compare(res1, res2)
            } else {
                costOne.compareTo(costTwo)
            }
        }
        var ratingComparator = Comparator<Restaurant> { res1, res2 ->
            if (res1.restaurantRating.compareTo(res2.restaurantRating) == 0) {
                res2.restaurantName.compareTo(res1.restaurantName)
            } else {
                res1.restaurantRating.compareTo(res2.restaurantRating)
            }
        }
    }
}