package guesswho;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class GameData {
    private static final double YES = 0.97;
    private static final double NO = 0.03;

    record Person(String name, Map<String, Double> traits) {
        double probabilityFor(String trait) {
            return traits.getOrDefault(trait, NO);
        }
    }

    record Question(String id, String text, String topic) {}

    static final List<Question> QUESTIONS = List.of(
        question("fictional", "Is this person fictional?"),
        question("alive", "Is this person alive today?"),
        question("female", "Is this person female?"),
        question("athlete", "Did this person become famous through sport?"),
        question("musician", "Is music the main reason this person is famous?"),
        question("actor", "Are they best known for acting?"),
        question("public_leader", "Are they known for politics or public leadership?"),
        question("business", "Are they mainly associated with business or technology?"),
        question("internet_creator", "Did they first become famous through the internet?"),
        question("indian", "Are they strongly associated with India?"),
        question("american", "Are they strongly associated with the United States?"),
        question("european", "Are they from Europe?"),
        question("latin", "Are they from a Spanish- or Portuguese-speaking country?"),
        question("famous_before_2000", "Were they already famous before the year 2000?"),
        question("rose_after_2010", "Did they become widely famous after 2010?"),
        question("under_40", "Are they younger than 40?"),
        question("team_sport", "Are they known for a team sport?"),
        question("football", "Are they closely associated with football or soccer?"),
        question("cricket", "Are they closely associated with cricket?"),
        question("tennis", "Are they closely associated with tennis?"),
        question("basketball", "Are they closely associated with basketball?"),
        question("film", "Are movies central to their fame?"),
        question("television", "Is television central to their fame?"),
        question("solo_music", "Are they mainly known as a solo music artist?"),
        question("band_member", "Were they famous as part of a music group or band?"),
        question("action_roles", "Are they especially known for action roles?"),
        question("singer_actor", "Are they well known for both singing and acting?"),
        question("social_media", "Do they have an exceptionally large social-media presence?"),
        question("award_winner", "Have they won a major international entertainment award?"),
        question("royal", "Are they connected to royalty?"),
        question("scientist", "Are they famous for science or invention?"),
        question("superhero", "Are they a superhero character?"),
        question("animated", "Are they an animated character?"),
        question("male", "Is this person male?"),
question("asian", "Are they from Asia?"),
question("youtube", "Are they mainly known through YouTube?"),
question("technology", "Are they mainly associated with technology?"),
question("rapper", "Are they known as a rapper?"),
question("comedy", "Are they mainly known for comedy?"),
question("marvel", "Is this character from Marvel?"),
question("dc", "Is this character from DC?"),
question("wrestling", "Are they associated with professional wrestling?"),
question("rose_after_2020", "Did they become famous after 2020?"),
        question("child_star", "Did they become famous as a child or teenager?"),
        question("over_50", "Are they older than 50?"),
        question("over_70", "Are they older than 70?"),
        question("deceased", "Has this person passed away?"),
        question("north_american", "Are they from North America?"),
        question("south_american", "Are they from South America?"),
        question("african", "Are they from Africa?"),
        question("middle_eastern", "Are they from the Middle East?"),
        question("australian", "Are they from Australia or New Zealand?"),
        question("british", "Are they British?"),
        question("canadian", "Are they Canadian?"),
        question("korean", "Are they Korean?"),
        question("japanese", "Are they Japanese?"),
        question("chinese", "Are they Chinese?"),
        question("spanish_speaking", "Are they from a Spanish-speaking country?"),
        question("portuguese_speaking", "Are they from a Portuguese-speaking country?"),
        question("hindi_language", "Are they strongly associated with Hindi-language media?"),
        question("tamil_language", "Are they strongly associated with Tamil-language media?"),
        question("telugu_language", "Are they strongly associated with Telugu-language media?"),
        question("kannada_language", "Are they strongly associated with Kannada-language media?"),
        question("english_language", "Are they strongly associated with English-language media?"),
        question("korean_language", "Are they strongly associated with Korean-language media?"),
        question("japanese_language", "Are they strongly associated with Japanese-language media?"),
        question("bollywood", "Are they strongly associated with Bollywood?"),
        question("south_indian_cinema", "Are they known for South Indian cinema?"),
        question("hollywood", "Are they strongly associated with Hollywood?"),
        question("anime", "Are they associated with anime?"),
        question("gaming", "Are they mainly known through gaming?"),
        question("streamer", "Are they a streamer?"),
        question("twitch", "Are they strongly associated with Twitch?"),
        question("instagram", "Are they especially famous on Instagram?"),
        question("tiktok", "Are they especially famous on TikTok?"),
        question("podcast", "Are they known for podcasting?"),
        question("director", "Are they known as a film director?"),
        question("producer", "Are they known as a producer?"),
        question("writer", "Are they known as a writer or author?"),
        question("dancer", "Are they known for dancing?"),
        question("model", "Are they known as a model?"),
        question("reality_tv", "Are they known for reality TV?"),
        question("talk_show", "Are they known for hosting or talk shows?"),
        question("news_media", "Are they known for news or journalism?"),
        question("standup_comedy", "Are they a stand-up comedian?"),
        question("voice_actor", "Are they known for voice acting?"),
        question("villain", "Is this person or character known as a villain?"),
        question("hero", "Is this person or character known as a hero?"),
        question("antihero", "Are they known as an antihero?"),
        question("disney", "Are they connected to Disney?"),
        question("pixar", "Are they connected to Pixar?"),
        question("star_wars", "Are they connected to Star Wars?"),
        question("harry_potter_franchise", "Are they connected to the Harry Potter franchise?"),
        question("lord_of_the_rings", "Are they connected to The Lord of the Rings?"),
        question("pokemon", "Are they connected to Pokemon?"),
        question("dragon_ball", "Are they connected to Dragon Ball?"),
        question("naruto_franchise", "Are they connected to Naruto?"),
        question("one_piece", "Are they connected to One Piece?"),
        question("bts", "Are they a member of BTS?"),
        question("blackpink", "Are they a member of BLACKPINK?"),
        question("stray_kids", "Are they a member of Stray Kids?"),
        question("kpop", "Are they associated with K-pop?"),
        question("bts_leader", "Are they the leader of BTS?"),
        question("bts_youngest", "Are they the youngest member of BTS?"),
        question("bts_oldest", "Are they the oldest member of BTS?"),
        question("main_dancer", "Are they especially known as a main dancer?"),
        question("main_vocalist", "Are they especially known as a main vocalist?"),
        question("visual_member", "Are they especially known as a visual member?"),
        question("deep_voice", "Are they known for a very deep voice?"),
        question("music_producer", "Are they known for producing music?"),
        question("thai", "Are they Thai?"),
        question("new_zealand", "Are they strongly associated with New Zealand?"),
        question("grammy_winner", "Have they won a Grammy?"),
        question("oscar_winner", "Have they won an Oscar?"),
        question("nobel_prize", "Have they won a Nobel Prize?"),
        question("olympic_medalist", "Have they won an Olympic medal?"),
        question("world_cup_winner", "Have they won a World Cup?"),
        question("billionaire", "Are they a billionaire?"),
        question("ceo", "Are they known as a CEO?"),
        question("founder", "Are they known as a company founder?"),
        question("inventor", "Are they known as an inventor?"),
        question("space", "Are they associated with space or rockets?"),
        question("software", "Are they associated with software?"),
        question("smartphones", "Are they associated with smartphones or gadgets?"),
        question("electric_cars", "Are they associated with electric cars?"),
        question("social_network", "Are they associated with a social network company?"),
        question("president", "Have they been a president?"),
        question("prime_minister", "Have they been a prime minister?"),
        question("freedom_movement", "Are they connected to a freedom movement?"),
        question("religious_leader", "Are they known as a religious or spiritual leader?"),
        question("royal_family", "Are they part of a royal family?"),
        question("historical_figure", "Are they mainly a historical figure?"),
        question("physics", "Are they associated with physics?"),
        question("biology", "Are they associated with biology or medicine?"),
        question("mathematics", "Are they associated with mathematics?"),
        question("chemistry", "Are they associated with chemistry?"),
        question("formula_1", "Are they associated with Formula 1?"),
        question("boxing", "Are they associated with boxing?"),
        question("mma", "Are they associated with mixed martial arts?"),
        question("golf", "Are they associated with golf?"),
        question("badminton", "Are they associated with badminton?"),
        question("swimming", "Are they associated with swimming?"),
        question("track_and_field", "Are they associated with track and field?"),
        question("captain", "Are they known for being a team captain?"),
        question("goalkeeper", "Are they a goalkeeper?"),
        question("bowler", "Are they known as a bowler?"),
        question("batter", "Are they known as a batter?"),
        question("all_rounder", "Are they known as an all-rounder?"),
        question("left_handed", "Are they known for being left-handed?"),
        question("married", "Are they married?"),
        question("has_children", "Do they have children?"),
        question("famous_family", "Are they from a famous family?"),
        question("siblings_famous", "Do they have famous siblings?"),
        question("short_hair", "Are they known for short hair?"),
        question("long_hair", "Are they known for long hair?"),
        question("blonde_hair", "Are they known for blonde hair?"),
        question("black_hair", "Are they known for black hair?"),
        question("beard", "Are they known for having a beard?"),
        question("glasses", "Are they known for wearing glasses?"),
        question("tattoos", "Are tattoos part of their public image?"),
        question("masked", "Are they known for wearing a mask?"),
        question("plays_guitar", "Are they known for playing guitar?"),
        question("plays_piano", "Are they known for playing piano?"),
        question("songwriter", "Are they known as a songwriter?"),
        question("album_artist", "Are albums central to their fame?"),
        question("music_group_leader", "Are they a leader of a music group?"),
        question("korean_idol", "Are they a Korean idol?"),
        question("rap_group_member", "Are they a rapper in a group?"),
        question("movie_franchise", "Are they strongly tied to a movie franchise?"),
        question("superpower", "Does this character have superpowers?"),
        question("detective", "Are they known as a detective?"),
        question("wizard", "Are they known as a wizard or magic user?"),
        question("robot", "Are they a robot or artificial being?"),
        question("animal_character", "Are they an animal character?"),
        question("video_game_character", "Are they a video game character?"),
        question("anime_ninja", "Are they an anime ninja?"),
        question("anime_pirate", "Are they an anime pirate?"),
        question("saiyan", "Are they a Saiyan?"),
        question("cartoon_network", "Are they connected to Cartoon Network?"),
        question("nickelodeon", "Are they connected to Nickelodeon?"),
        question("netflix", "Are they associated with Netflix?"),
        question("hbo", "Are they associated with HBO?"),
        question("amazon_prime", "Are they associated with Amazon Prime Video?"),
        question("football_forward", "Are they a football forward?"),
        question("football_midfielder", "Are they a football midfielder?"),
        question("football_defender", "Are they a football defender?"),
        question("nba_player", "Are they an NBA player?"),
        question("wwe", "Are they associated with WWE?"),
        question("ufc", "Are they associated with UFC?"),
        question("grand_slam_winner", "Have they won a tennis Grand Slam?"),
        question("f1_champion", "Have they been a Formula 1 champion?"),
        question("ipl", "Are they associated with the IPL?"),
        question("world_record", "Are they known for a world record?"),
        question("meme_famous", "Are they often used in internet memes?"),
        question("controversial", "Are they publicly controversial?"),
        question("philanthropy", "Are they known for philanthropy?"),
        question("fashion", "Are they associated with fashion?"),
        question("beauty_brand", "Are they associated with a beauty brand?"),
        question("fitness", "Are they associated with fitness?"),
        question("cooking", "Are they known for cooking or food content?"),
        question("education", "Are they known for education content?"),
        question("standalone_character", "Are they mainly known as a standalone character?")
    );

    static final List<Person> PEOPLE = List.of(
        person("Taylor Swift",
    "alive", "female", "musician", "american", "under_40", "solo_music",
    "social_media", "award_winner"),

person("Beyonce",
    "alive", "female", "musician", "american", "solo_music", "singer_actor",
    "social_media", "award_winner", "famous_before_2000:0.65"),

person("Ariana Grande",
    "alive", "female", "musician", "american", "under_40", "solo_music",
    "singer_actor", "rose_after_2010", "social_media", "award_winner"),

person("Ed Sheeran",
    "alive", "musician", "european", "under_40:0.65", "solo_music",
    "rose_after_2010", "social_media", "award_winner"),

person("Michael Jackson",
    "deceased", "musician", "american", "famous_before_2000", "solo_music",
    "band_member:0.65", "award_winner", "main_dancer", "grammy_winner",
    "music_producer:0.55"),

person("Freddie Mercury",
    "deceased", "musician", "european", "famous_before_2000", "band_member",
    "main_vocalist", "award_winner"),

person("Selena Gomez",
    "alive", "female", "musician:0.75", "actor:0.7", "american", "under_40",
    "solo_music", "film:0.45", "television", "singer_actor", "social_media",
    "award_winner"),

person("Shah Rukh Khan",
    "alive", "actor", "indian", "famous_before_2000", "film", "social_media",
    "bollywood", "hindi_language", "award_winner"),

person("Amitabh Bachchan",
    "alive", "actor", "indian", "famous_before_2000", "film", "television:0.6",
    "bollywood", "hindi_language", "award_winner"),

person("Priyanka Chopra",
    "alive", "female", "actor", "indian", "american:0.55", "film",
    "television:0.45", "bollywood", "hindi_language", "hollywood:0.55",
    "singer_actor:0.6", "social_media", "award_winner"),

person("Deepika Padukone",
    "alive", "female", "actor", "indian", "under_40:0.55", "film",
    "bollywood", "hindi_language", "social_media", "award_winner"),

person("Tom Cruise",
    "alive", "actor", "american", "famous_before_2000", "film",
    "action_roles", "award_winner"),

person("Emma Watson",
    "alive", "female", "actor", "european", "under_40:0.55", "film",
    "award_winner"),

person("Dwayne Johnson",
    "alive", "actor", "athlete:0.6", "american", "film", "action_roles",
    "social_media", "award_winner:0.55"),

person("Jackie Chan",
    "alive", "actor", "asian", "famous_before_2000", "film", "action_roles",
    "award_winner"),

person("Lionel Messi",
    "alive", "athlete", "latin", "team_sport", "football", "social_media",
    "award_winner"),

person("Cristiano Ronaldo",
    "alive", "athlete", "european", "team_sport", "football", "social_media",
    "award_winner"),

person("Virat Kohli",
    "alive", "athlete", "indian", "under_40:0.6", "team_sport", "cricket",
    "batter", "captain", "ipl", "social_media", "award_winner"),

person("Sachin Tendulkar",
    "alive", "athlete", "indian", "famous_before_2000", "team_sport", "cricket",
    "batter", "captain:0.55", "world_record", "award_winner"),

person("Serena Williams",
    "alive", "female", "athlete", "american", "famous_before_2000:0.55",
    "tennis", "social_media", "award_winner"),

person("LeBron James",
    "alive", "athlete", "american", "team_sport", "basketball",
    "social_media", "award_winner"),

person("Elon Musk",
    "alive", "business", "american:0.65", "social_media",
    "award_winner:0.4"),

person("Steve Jobs",
    "deceased", "business", "american", "famous_before_2000",
    "technology", "ceo", "founder", "smartphones", "award_winner:0.45"),

person("Oprah Winfrey",
    "alive", "female", "television", "business:0.65", "american",
    "famous_before_2000", "award_winner"),

person("MrBeast",
    "alive", "internet_creator", "american", "under_40",
    "rose_after_2010", "social_media", "business:0.55",
    "award_winner:0.55"),

person("Kim Kardashian",
    "alive", "female", "television", "business:0.7", "american",
    "social_media", "award_winner:0.45"),

person("Barack Obama",
    "alive", "public_leader", "american",
    "famous_before_2000:0.35", "social_media", "award_winner"),

person("Narendra Modi",
    "alive", "public_leader", "indian",
    "social_media", "award_winner:0.55"),

person("Albert Einstein",
    "deceased", "scientist", "european", "american:0.45",
    "famous_before_2000", "historical_figure", "physics", "nobel_prize",
    "award_winner"),

person("Princess Diana",
    "deceased", "female", "royal", "european",
    "famous_before_2000", "royal_family", "historical_figure",
    "award_winner:0.4"),

person("Harry Potter",
    "fictional", "european", "under_40:0.65",
    "film", "harry_potter_franchise", "wizard", "hero",
    "award_winner:0.4"),

person("Spider-Man",
    "fictional", "american", "under_40:0.75",
    "film", "superhero", "action_roles", "marvel", "hero",
    "superpower"),

person("Mickey Mouse",
    "fictional", "american", "famous_before_2000",
    "film:0.55", "television", "animated", "disney",
    "animal_character", "standalone_character"),

// -------- NEW ADDITIONS START HERE --------

person("Ranbir Kapoor",
    "alive", "actor", "indian", "film",
    "bollywood", "hindi_language", "famous_family",
    "social_media", "award_winner"),

person("Ranveer Singh",
    "alive", "actor", "indian", "film",
    "bollywood", "hindi_language", "social_media", "award_winner"),

person("Alia Bhatt",
    "alive", "female", "actor", "indian",
    "under_40", "film", "bollywood", "hindi_language", "famous_family",
    "social_media",
    "award_winner"),

person("Katrina Kaif",
    "alive", "female", "actor",
    "indian:0.6", "film", "bollywood", "hindi_language",
    "social_media",
    "award_winner"),

person("Anushka Sharma",
    "alive", "female", "actor", "indian",
    "film", "bollywood", "hindi_language", "social_media", "award_winner"),

person("Kriti Sanon",
    "alive", "female", "actor", "indian",
    "under_40", "film", "bollywood", "hindi_language", "social_media",
    "award_winner"),

person("Kiara Advani",
    "alive", "female", "actor", "indian",
    "under_40", "film", "bollywood", "hindi_language", "social_media"),

person("Shraddha Kapoor",
    "alive", "female", "actor", "indian",
    "under_40", "film", "bollywood", "hindi_language", "famous_family",
    "social_media",
    "award_winner"),

person("Salman Khan",
    "alive", "actor", "indian",
    "film", "bollywood", "hindi_language", "famous_before_2000",
    "social_media", "award_winner"),

person("Aamir Khan",
    "alive", "actor", "indian",
    "film", "bollywood", "hindi_language", "famous_before_2000",
    "award_winner"),

person("Akshay Kumar",
    "alive", "actor", "indian",
    "film", "bollywood", "hindi_language", "action_roles",
    "award_winner"),

person("Hrithik Roshan",
    "alive", "actor", "indian",
    "film", "bollywood", "hindi_language", "action_roles",
    "social_media", "award_winner"),

person("Allu Arjun",
    "alive", "actor", "indian",
    "film", "south_indian_cinema", "telugu_language", "action_roles",
    "social_media", "award_winner"),

person("Prabhas",
    "alive", "actor", "indian",
    "film", "south_indian_cinema", "telugu_language", "action_roles",
    "award_winner"),

person("Ram Charan",
    "alive", "actor", "indian",
    "film", "south_indian_cinema", "telugu_language", "social_media",
    "award_winner"),

person("Jr NTR",
    "alive", "actor", "indian",
    "film", "south_indian_cinema", "telugu_language", "award_winner"),

person("Yash",
    "alive", "actor", "indian",
    "film", "south_indian_cinema", "kannada_language", "action_roles",
    "social_media"),

person("Mahesh Babu",
    "alive", "actor", "indian",
    "film", "south_indian_cinema", "telugu_language", "social_media"),

person("Rashmika Mandanna",
    "alive", "female", "actor",
    "indian", "under_40",
    "film", "south_indian_cinema", "telugu_language", "social_media"),

person("Samantha Ruth Prabhu",
    "alive", "female", "actor",
    "indian", "film", "south_indian_cinema", "telugu_language:0.75",
    "tamil_language:0.65",
    "social_media", "award_winner"),
    // -------- PART 2 (People 54–73) --------

person("MS Dhoni",
    "alive", "athlete", "indian", "team_sport", "cricket",
    "famous_before_2000:0.45", "captain", "batter", "ipl",
    "social_media", "award_winner"),

person("Rohit Sharma",
    "alive", "athlete", "indian", "team_sport", "cricket",
    "captain", "batter", "ipl", "social_media", "award_winner"),

person("Hardik Pandya",
    "alive", "athlete", "indian", "under_40",
    "team_sport", "cricket", "all_rounder", "ipl", "social_media"),

person("Jasprit Bumrah",
    "alive", "athlete", "indian", "under_40:0.7",
    "team_sport", "cricket", "bowler", "ipl", "award_winner"),

person("KL Rahul",
    "alive", "athlete", "indian", "under_40",
    "team_sport", "cricket", "batter", "ipl", "social_media"),

person("Shubman Gill",
    "alive", "athlete", "indian", "under_40",
    "team_sport", "cricket", "rose_after_2020",
    "batter", "ipl", "social_media"),

person("Rishabh Pant",
    "alive", "athlete", "indian", "under_40",
    "team_sport", "cricket", "batter", "left_handed", "ipl",
    "social_media"),

person("Suryakumar Yadav",
    "alive", "athlete", "indian",
    "team_sport", "cricket", "batter", "ipl", "social_media"),

person("Neeraj Chopra",
    "alive", "athlete", "indian",
    "award_winner", "social_media"),

person("PV Sindhu",
    "alive", "female", "athlete",
    "indian", "award_winner", "social_media"),

person("Neymar Jr",
    "alive", "athlete", "latin",
    "team_sport", "football",
    "social_media", "award_winner"),

person("Kylian Mbappe",
    "alive", "athlete", "european",
    "under_40", "team_sport",
    "football", "social_media"),

person("Erling Haaland",
    "alive", "athlete", "european",
    "under_40", "team_sport",
    "football", "social_media"),

person("Jungkook",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "main_vocalist", "bts_youngest", "solo_music:0.5", "social_media",
    "award_winner"),

person("V",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "visual_member", "deep_voice", "actor:0.45", "social_media"),

person("Jimin",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "main_dancer", "main_vocalist:0.55", "social_media"),

person("RM",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "bts_leader", "rapper", "rap_group_member", "songwriter",
    "music_producer", "award_winner"),

person("Jin",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "bts_oldest", "visual_member", "main_vocalist:0.45"),

person("Suga",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "rapper", "rap_group_member", "songwriter", "music_producer"),

person("J-Hope",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "bts", "kpop", "korean_idol", "korean_language",
    "rapper", "rap_group_member", "main_dancer", "songwriter",
    "social_media"),
    // -------- PART 3 (People 74–93) --------

person("Jennie",
    "alive", "female", "musician",
    "asian", "korean", "under_40",
    "band_member", "blackpink", "kpop", "korean_idol", "korean_language",
    "rapper", "solo_music:0.55", "fashion", "social_media",
    "award_winner"),

person("Lisa",
    "alive", "female", "musician",
    "asian", "thai", "korean:0.45", "under_40",
    "band_member", "blackpink", "kpop", "korean_idol", "korean_language",
    "main_dancer", "rapper", "solo_music:0.55", "social_media",
    "award_winner"),

person("Rose",
    "alive", "female", "musician",
    "asian", "korean", "new_zealand", "under_40",
    "band_member", "blackpink", "kpop", "korean_idol", "korean_language",
    "main_vocalist", "solo_music:0.55", "plays_guitar", "social_media"),

person("Jisoo",
    "alive", "female", "musician",
    "asian", "korean", "under_40",
    "band_member", "blackpink", "kpop", "korean_idol", "korean_language",
    "visual_member", "actor:0.65", "social_media"),

person("Hyunjin",
    "alive", "male", "musician",
    "asian", "korean", "under_40",
    "band_member", "stray_kids", "kpop", "korean_idol", "korean_language",
    "main_dancer", "visual_member", "social_media"),

person("Felix",
    "alive", "male", "musician",
    "asian", "korean", "australian", "under_40",
    "band_member", "stray_kids", "kpop", "korean_idol", "korean_language",
    "deep_voice", "main_dancer:0.65", "social_media"),

person("Bang Chan",
    "alive", "male", "musician",
    "asian", "korean", "australian", "under_40",
    "band_member", "stray_kids", "kpop", "korean_idol", "korean_language",
    "music_group_leader", "rapper:0.55", "songwriter", "music_producer",
    "award_winner"),

person("Billie Eilish",
    "alive", "female", "musician",
    "american", "under_40",
    "solo_music", "social_media",
    "award_winner"),

person("Justin Bieber",
    "alive", "male", "musician",
    "american", "under_40",
    "solo_music", "social_media",
    "award_winner"),

person("Bruno Mars",
    "alive", "male", "musician",
    "american", "solo_music",
    "award_winner"),

person("Dua Lipa",
    "alive", "female", "musician",
    "european", "under_40",
    "solo_music", "social_media"),

person("The Weeknd",
    "alive", "male", "musician",
    "american:0.5", "solo_music",
    "social_media", "award_winner"),

person("Olivia Rodrigo",
    "alive", "female", "musician",
    "american", "under_40",
    "solo_music", "rose_after_2020",
    "social_media"),

person("Sabrina Carpenter",
    "alive", "female", "musician",
    "american", "under_40",
    "solo_music", "rose_after_2020",
    "social_media"),

person("CarryMinati",
    "alive", "internet_creator",
    "indian", "under_40",
    "youtube", "social_media",
    "award_winner"),

person("Triggered Insaan",
    "alive", "internet_creator",
    "indian", "under_40",
    "youtube", "social_media"),

person("Bhuvan Bam",
    "alive", "internet_creator",
    "indian", "under_40",
    "youtube", "musician:0.45",
    "social_media"),

person("Ashish Chanchlani",
    "alive", "internet_creator",
    "indian", "under_40",
    "youtube", "comedy",
    "social_media"),

person("Tech Burner",
    "alive", "internet_creator",
    "indian", "under_40",
    "youtube", "technology",
    "social_media"),

person("Mrwhosetheboss",
    "alive", "internet_creator",
    "european", "under_40",
    "youtube", "technology",
    "social_media"),
    // -------- PART 4 (People 94–100) --------

person("Iron Man",
    "fictional", "american",
    "film", "superhero",
    "action_roles", "marvel", "hero",
    "technology"),

person("Batman",
    "fictional", "american",
    "film", "superhero",
    "action_roles", "dc", "hero",
    "detective"),

person("Naruto Uzumaki",
    "fictional", "asian", "japanese_language",
    "animated", "anime", "naruto_franchise", "anime_ninja",
    "action_roles", "hero", "superpower",
    "under_40:0.75"),

person("Monkey D. Luffy",
    "fictional", "asian", "japanese_language",
    "animated", "anime", "one_piece", "anime_pirate",
    "action_roles", "hero", "superpower",
    "under_40:0.75"),

person("Goku",
    "fictional", "asian", "japanese_language",
    "animated", "anime", "dragon_ball", "saiyan",
    "action_roles", "hero", "superpower",
    "famous_before_2000"),

person("John Cena",
    "alive", "athlete",
    "american", "wrestling",
    "television", "social_media",
    "award_winner"),

person("Sundar Pichai",
    "alive", "business",
    "indian", "american:0.55",
    "technology", "award_winner:0.4")
    );

    static final Map<String, Question> QUESTION_BY_ID = buildQuestionIndex();
    static final Map<String, Person> PERSON_BY_NAME = buildPersonIndex();

    static {
        validate();
    }

    private GameData() {}

    private static Question question(String id, String text) {
        return new Question(id, text, questionTopic(id));
    }

    private static String questionTopic(String id) {
        if (Set.of(
            "bts_leader",
            "bts_youngest",
            "bts_oldest",
            "main_dancer",
            "main_vocalist",
            "visual_member",
            "deep_voice",
            "music_producer",
            "anime_ninja",
            "anime_pirate",
            "saiyan"
        ).contains(id)) {
            return id;
        }

        return id.contains("_") ? id.substring(0, id.indexOf('_')) : id;
    }

    private static Map<String, Question> buildQuestionIndex() {
        Map<String, Question> questions = new LinkedHashMap<>();

        for (Question question : QUESTIONS) {
            if (questions.put(question.id(), question) != null) {
                throw new IllegalStateException("Duplicate question id: " + question.id());
            }
        }

        return Map.copyOf(questions);
    }

    private static Map<String, Person> buildPersonIndex() {
        Map<String, Person> people = new LinkedHashMap<>();

        for (Person person : PEOPLE) {
            if (people.put(person.name(), person) != null) {
                throw new IllegalStateException("Duplicate person: " + person.name());
            }
        }

        return Map.copyOf(people);
    }

    private static void validate() {
        Set<String> questionIds = new HashSet<>(QUESTION_BY_ID.keySet());

        for (Person person : PEOPLE) {
            if (person.traits().size() < 4) {
                throw new IllegalStateException(person.name() + " has too few traits.");
            }

            if (person.traits().size() > 35) {
                throw new IllegalStateException(person.name() + " has too many traits.");
            }

            for (Map.Entry<String, Double> trait : person.traits().entrySet()) {
                if (!questionIds.contains(trait.getKey())) {
                    throw new IllegalStateException(
                        person.name() + " uses unknown trait: " + trait.getKey()
                    );
                }

                if (trait.getValue() < 0.0 || trait.getValue() > 1.0) {
                    throw new IllegalStateException(
                        person.name() + " has invalid probability for " + trait.getKey()
                    );
                }
            }
        }
    }

    private static Person person(String name, String... traitSpecs) {
        Map<String, Double> traits = new LinkedHashMap<>();

        for (String specification : traitSpecs) {
            String[] parts = specification.split(":", 2);
            double probability = parts.length == 2 ? Double.parseDouble(parts[1]) : YES;
            traits.put(parts[0], probability);
        }

        return new Person(name, Map.copyOf(traits));
    }
}
