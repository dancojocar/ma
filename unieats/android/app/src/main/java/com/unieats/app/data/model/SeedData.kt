package com.unieats.app.data.model

private fun photo(id: String) = "https://picsum.photos/seed/$id/400/300"

val seedSpots: List<Spot> = listOf(
    Spot("spot-1", "Central Canteen", Category.CANTEEN, 3.8, 1, 44.4268, 26.1025, true, photo("spot-1"), "The main campus canteen with daily hot meals.", 1700000000000),
    Spot("spot-2", "Espresso Lab", Category.CAFE, 4.5, 2, 44.4275, 26.1030, true, photo("spot-2"), "Specialty coffee and pastries near the library.", 1700000001000),
    Spot("spot-3", "Pizza Stop", Category.FASTFOOD, 4.1, 2, 44.4260, 26.1015, true, photo("spot-3"), "Pizza by the slice for students on the go.", 1700000002000),
    Spot("spot-4", "Bread & Butter", Category.BAKERY, 4.7, 1, 44.4280, 26.1040, false, photo("spot-4"), "Fresh bread and pastries baked every morning.", 1700000003000),
    Spot("spot-5", "The Pub Garden", Category.BAR, 4.2, 3, 44.4255, 26.1010, false, photo("spot-5"), "Outdoor bar with craft beers and snacks.", 1700000004000),
    Spot("spot-6", "Sushi Box", Category.FASTFOOD, 3.9, 2, 44.4270, 26.1050, true, photo("spot-6"), "Grab-and-go sushi rolls and bento boxes.", 1700000005000),
    Spot("spot-7", "Campus Bistro", Category.CAFE, 4.3, 2, 44.4265, 26.1035, true, photo("spot-7"), "Relaxed cafe with sandwiches, salads and wifi.", 1700000006000),
    Spot("spot-8", "Grandma's Kitchen", Category.CANTEEN, 4.6, 1, 44.4272, 26.1020, true, photo("spot-8"), "Traditional home-cooked Romanian meals.", 1700000007000)
)

val seedReviews: List<Review> = listOf(
    Review("review-1", "spot-1", "Ana", 4, "Good value for money, the soup is always hot.", 1700000100000),
    Review("review-2", "spot-1", "Mihai", 3, "Long queue at noon, go early.", 1700000150000),
    Review("review-3", "spot-2", "Ioana", 5, "Best flat white on campus!", 1700000200000),
    Review("review-4", "spot-3", "Andrei", 4, "Crispy crust every time.", 1700000300000)
)
