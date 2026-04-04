//package com.example.dacs3_ticket_booking_app.data
//
//import com.example.dacs3_ticket_booking_app.data.model.Movie
//import com.example.dacs3_ticket_booking_app.data.model.Cast
//import com.example.dacs3_ticket_booking_app.data.repository.MovieRepository
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//object DatabaseInitializer {
//    fun initializeMockData() {
//        val movieRepository = MovieRepository()
//
//        CoroutineScope(Dispatchers.IO).launch {
//
//            // Create mock movies
//            val mockMovies = listOf(
//                Movie(
//                    title = "Avengers: Endgame",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "The Avengers assemble to defeat Thanos",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976723/tMO0YLXgJZBnIAjoTSz26zE33YN_avwtjn.jpg",
//                    status = "coming_soon",
//                    releaseDate = "2024-05-15",
//                    duration = 181.0,
//                    casts = listOf(
//                        Cast(name = "Robert Downey Jr.", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976723/actor1_abc123.jpg"),
//                        Cast(name = "Chris Evans", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976723/actor2_def456.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Spider-Man: No Way Home",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "Peter Parker faces new threats",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976716/madmax_nrvq7x.jpg",
//                    status = "now_showing",
//                    releaseDate = "2024-04-01",
//                    duration = 160.0,
//                    casts = listOf(
//                        Cast(name = "Tom Holland", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976716/actor3_ghi789.jpg"),
//                        Cast(name = "Zendaya", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976716/actor4_jkl012.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Black Panther",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "The king of Wakanda returns",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976713/hu40Uxp9WtpL34jv3zyWLb5zEVY_q28zam.jpg",
//                    status = "coming_soon",
//                    releaseDate = "2024-06-20",
//                    duration = 180.0,
//                    casts = listOf(
//                        Cast(name = "Chadwick Boseman", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976713/actor5_mno345.jpg"),
//                        Cast(name = "Letitia Wright", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976713/actor6_pqr678.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Guardians of the Galaxy Vol. 3",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "The Guardians face new challenges",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976711/kDp1vUBnMpe8ak4rjgl3cLELqjU_cuu3xp.jpg",
//                    status = "coming_soon",
//                    releaseDate = "2024-07-10",
//                    duration = 150.0,
//                    casts = listOf(
//                        Cast(name = "Chris Pratt", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976711/actor7_stu901.jpg"),
//                        Cast(name = "Zoe Saldana", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976711/actor8_vwx234.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Doctor Strange in the Multiverse of Madness",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "Doctor Strange explores the multiverse",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976708/gmGK5Gw5CIGMPhOmTO0bNA9Q66c_tfbnjp.jpg",
//                    status = "now_showing",
//                    releaseDate = "2024-03-25",
//                    duration = 130.0,
//                    casts = listOf(
//                        Cast(name = "Benedict Cumberbatch", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976708/actor9_yza567.jpg"),
//                        Cast(name = "Elizabeth Olsen", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976708/actor10_bcd890.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Thor: Love and Thunder",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "Thor faces new threats with Jane Foster",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976703/Fly_Me_to_the_Moon_i8zr79.jpg",
//                    status = "ended",
//                    releaseDate = "2024-02-14",
//                    duration = 120.0,
//                    casts = listOf(
//                        Cast(name = "Chris Hemsworth", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976703/actor11_efg123.jpg"),
//                        Cast(name = "Natalie Portman", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976703/actor12_hij456.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Captain Marvel",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "Carol Danvers becomes Captain Marvel",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/Dune_Part_Two_xeolqd.jpg",
//                    status = "ended",
//                    releaseDate = "2024-01-10",
//                    duration = 110.0,
//                    casts = listOf(
//                        Cast(name = "Brie Larson", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/actor13_klm789.jpg"),
//                        Cast(name = "Samuel L. Jackson", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/actor14_nop012.jpg")
//                    )
//                ),
//                Movie(
//                    title = "The Batman",
//                    genres = listOf("Action", "Crime"),
//                    year = 2024,
//                    description = "Batman uncovers corruption in Gotham City",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976726/wide8_kvqjjg.jpg",
//                    status = "now_showing",
//                    releaseDate = "2024-04-10",
//                    duration = 175.0,
//                    casts = listOf(
//                        Cast(name = "Robert Pattinson", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976726/actor15_qrs345.jpg"),
//                        Cast(name = "Zoë Kravitz", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976726/actor16_tuv678.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Fast & Furious 10",
//                    genres = listOf("Action", "Thriller"),
//                    year = 2024,
//                    description = "Dom Toretto faces his most dangerous enemy",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/Dune_Part_Two_xeolqd.jpg",
//                    status = "coming_soon",
//                    releaseDate = "2024-06-05",
//                    duration = 140.0,
//                    casts = listOf(
//                        Cast(name = "Vin Diesel", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/actor17_wxy901.jpg"),
//                        Cast(name = "Michelle Rodriguez", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/actor18_zab234.jpg")
//                    )
//                ),
//                Movie(
//                    title = "John Wick: Chapter 4",
//                    genres = listOf("Action", "Crime"),
//                    year = 2024,
//                    description = "John Wick takes on the High Table",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/Dune_Part_Two_xeolqd.jpg",
//                    status = "now_showing",
//                    releaseDate = "2024-03-15",
//                    duration = 169.0,
//                    casts = listOf(
//                        Cast(name = "Keanu Reeves", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/actor19_cde567.jpg"),
//                        Cast(name = "Laurence Fishburne", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976696/actor20_fgh890.jpg")
//                    )
//                ),
//                Movie(
//                    title = "The Flash",
//                    genres = listOf("Action", "Sci-Fi"),
//                    year = 2024,
//                    description = "Barry Allen travels through time to save his mother",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976681/Atlas_jxfvbu.jpg",
//                    status = "coming_soon",
//                    releaseDate = "2024-07-01",
//                    duration = 144.0,
//                    casts = listOf(
//                        Cast(name = "Ezra Miller", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976681/actor21_ijk123.jpg"),
//                        Cast(name = "Michael Keaton", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976681/actor22_lmn456.jpg")
//                    )
//                ),
//                Movie(
//                    title = "Aquaman and the Lost Kingdom",
//                    genres = listOf("Action", "Adventure"),
//                    year = 2024,
//                    description = "Aquaman returns to protect Atlantis",
//                    poster = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976679/aqKtSJdsUYNhEYfV42AYpamhEle_iiesf9.jpg",
//                    status = "ended",
//                    releaseDate = "2024-01-25",
//                    duration = 125.0,
//                    casts = listOf(
//                        Cast(name = "Jason Momoa", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976679/actor23_opr789.jpg"),
//                        Cast(name = "Amber Heard", images = "https://res.cloudinary.com/drmkkrmkw/image/upload/v1773976679/actor24_stu012.jpg")
//                    )
//                )
//
//            )
//
//
//            // Add movies to database
//            for (movie in mockMovies) {
//                movieRepository.addMovie(movie)
//            }
//
//            println("✅ Mock database initialized successfully")
//        }
//    }
//}
//
//
