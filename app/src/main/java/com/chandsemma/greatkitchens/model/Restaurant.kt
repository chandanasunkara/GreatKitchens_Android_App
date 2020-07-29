package com.chandsemma.greatkitchens.model

import java.io.Serializable

data class Restaurant(val restaurantId: Int,
                      val restaurantName: String,
                      val restaurantRating: String,
                      val costForOne: Int,
                      val resImageUrl: String)