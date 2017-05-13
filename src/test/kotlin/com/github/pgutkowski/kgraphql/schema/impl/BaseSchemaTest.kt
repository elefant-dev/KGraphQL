package com.github.pgutkowski.kgraphql.schema.impl

import com.github.pgutkowski.kgraphql.*
import com.github.pgutkowski.kgraphql.schema.dsl.SchemaBuilder
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import java.util.*


abstract class BaseSchemaTest {

    //test film 1
    val tomHardy = Actor("Tom Hardy", 232)
    val christianBale = Actor("Christian Bale", 232)
    val christopherNolan = Director("Christopher Nolan", 43, listOf(tomHardy, christianBale))
    val prestige = Film(Id("Prestige", 2006), 2006, "Prestige", christopherNolan)

    //test film 2
    val bradPitt = Actor("Brad Pitt", 763)
    val morganFreeman = Actor("Morgan Freeman", 1212)
    val kevinSpacey = Actor("Kevin Spacey", 2132)
    val davidFincher = Director("David Fincher", 43, listOf(bradPitt, morganFreeman, kevinSpacey))
    val se7en = Film(Id("Se7en", 1995), 1995, "Se7en", davidFincher)

    //new actors created via mutations in schema
    val createdActors = mutableListOf<Actor>()

    val testedSchema = SchemaBuilder {
        query {
            name = "film"
            description = "mock film"
            resolver { -> prestige }
        }
        query {
            name = "filmByRank"
            description = "ranked films"
            resolver { rank: Int -> when(rank){
                1 -> prestige
                2 -> se7en
                else -> null
            }}
        }
        query {
            name = "filmsByType"
            description = "film categorized by type"
            resolver {type: FilmType -> listOf(prestige, se7en) }
        }
        query {
            name = "people"
            description = "List of all people"
            resolver { -> listOf(davidFincher, bradPitt, morganFreeman, christianBale, christopherNolan) }
        }
        query {
            name = "randomPerson"
            description = "not really random person"
            resolver { -> davidFincher as Person /*not really random*/}
        }
        mutation {
            name = "createActor"
            description = "create new actor"
            resolver { name : String, age : Int ->
                val actor = Actor(name, age)
                createdActors.add(actor)
                actor
            }
        }
        query {
            name = "scenario"
            resolver { -> Scenario(Id("GKalus", 234234), "Gamil Kalus", "TOO LONG") }
        }
        supportedScalar<Id> {
            description = "unique, concise representation of film"
            support = IdScalarSupport()
        }
        enum<FilmType>{ description = "type of film, base on its length" }
        type<Person>{ description = "Common data for any person"}
        type<Scenario>{
            ignore(Scenario::author)
        }
    }.build() as DefaultSchema

    @Before
    fun cleanup() = createdActors.clear()

    fun assertNoErrors(map : Map<*,*>) {
        if(map["errors"] != null) throw AssertionError("Errors encountered: ${map["errors"]}")
        if(map["data"] == null) throw AssertionError("Data is null")
    }

    fun assertError(map : Map<*,*>, vararg messageElements : String) {
        val errorMessage = extract<String>(map, "errors/message")
        MatcherAssert.assertThat(errorMessage, CoreMatchers.notNullValue())

        messageElements
                .filterNot { errorMessage.contains(it) }
                .forEach { throw AssertionError("Expected error message to contain $it, but was: $errorMessage") }
    }

    fun execute(query: String, variables : String? = null) = deserialize(testedSchema.handleRequest(query, variables))
}