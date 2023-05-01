import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.util.*

object UserTable : Table<User>("users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
}

object Posts : Table<Post>("posts") {
    val id = int("id").primaryKey().bindTo { it.id }
    val content = text("content").bindTo { it.content }
    val userId = int("user_id").references(UserTable) { it.user }
}

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    val id: Int
    var username: String
    var password: String
}

interface Post : Entity<Post> {
    companion object : Entity.Factory<Post>()
    val id: Int
    var content: String
    var user: User
}

fun main() {
    val database = Database.connect(
        url = "jdbc:mysql://localhost:3306/simple_posts",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "Transcom1!"
    )

    println("Welcome to the Simple Posts Application!")

    var user: User? = null

    while (user == null) {
        println("Choose an option:")
        println("1. Register")
        println("2. Log in")

        when (readLine()) {
            "1" -> {
                print("Enter a username: ")
                val newUsername = readLine() ?: ""
                print("Enter a password: ")
                val newPassword = readLine() ?: ""

                val existingUser = database.sequenceOf(UserTable).firstOrNull { it.username eq newUsername }
                if (existingUser != null) {
                    println("Username already taken. Please try another one.")
                } else {
                    database.insert(UserTable) {
                        set(it.username, newUsername)
                        set(it.password, newPassword)
                    }
                    user = database.sequenceOf(UserTable).firstOrNull { it.username eq newUsername }
                    println("Registration successful. Logged in as ${user?.username}.")
                }
            }
            "2" -> {
                print("Username: ")
                val username = readLine() ?: ""
                print("Password: ")
                val password = readLine() ?: ""

                user = database.sequenceOf(UserTable).firstOrNull { (it.username eq username) and (it.password eq password) }

                if (user == null) {
                    println("Invalid credentials. Please try again.")
                } else {
                    println("Logged in as ${user.username}")
                }
            }
            else -> {
                println("Invalid option. Please try again.")
            }
        }
    }

    while (true) {
        println("Choose an option:")
        println("1. Create a new post")
        println("2. View all posts")
        println("3. Logout")

        when (readLine()) {
            "1" -> {
                print("Enter your post content: ")
                val content = readLine() ?: ""

                database.insert(Posts) {
                    set(it.content, content)
                    set(it.userId, user.id)
                }

                println("Post created.")
            }
            "2" -> {
                val posts = database.from(Posts).innerJoin(UserTable, on = Posts.userId eq UserTable.id).select()
                for (row in posts) {
                    val postContent = row[Posts.content]
                    val postAuthor = row[UserTable.username]
                    println("$postContent (by $postAuthor)")
                }
            }
            "3" -> {
                println("Logged out. Goodbye!")
                break
            }
            else -> {
                println("Invalid option. Please try again.")
            }
        }
    }
}

