val projectAttributes = mapOf(
    "Main-Class" to null ,
    "Project-Name" to "${projectDir.toString().let{it.substring( it.lastIndexOf("/")+1 )}}" ,
    "Jar-Name" to "${projectDir.toString().let{it.substring( it.lastIndexOf("/")+1 )}}.jar" ,
    "Extracted-Files" to "extractedFiles" ,
    "Output-Path" to "jar"
)

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    application
}

repositories { mavenCentral() }

application {
    mainClass.set( projectAttributes["Main-Class"] )
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")
    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val jar by tasks.getting(Jar::class){
    manifest{
        attributes["Main-Class"] = projectAttributes["Main-Class"].toString()
    }
}

tasks.register<Copy>("extract-jars"){
    dependsOn("installDist")
    val jarFiles =
        File( "$buildDir/install/${projectAttributes["Project-Name"]}/lib" ).let{
            if ( ! it.isDirectory ) Runtime.getRuntime().exec( "gradle installDist" ).waitFor()
            if ( ! it.isDirectory ) throw Exception("Incompaticle project\nDirectory Not Found : $it")
            it.listFiles()
        }
    for ( jar in jarFiles ){
        from( zipTree( jar ) ){
            if( ! jar.toString().contains(
                    projectAttributes["Jar-Name"]?.let{ it } ?: "null"
                ) ) exclude( "**/META-INF/" )
        }
        into( layout.buildDirectory.dir( projectAttributes["Extracted-Files"]?.let{ it } ?: "null" ) )
    }
}

tasks.register<Zip>("repack-Jar-With-Dependencies"){
    dependsOn( "extract-jars" )
    archiveFileName.set( projectAttributes["Jar-Name"] )
    destinationDirectory.set( layout.buildDirectory.dir( projectAttributes["Output-Path"]?.let{ it }?:"null" ) )
    from( layout.buildDirectory.dir( projectAttributes["Extracted-Files"]?.let{it}?:"null" ) )
}

tasks.register("packJar"){
    if( projectAttributes["Main-Class"] == null ) println("\n\nmain class not configured\nAdd path to main in build gradle file\n\n")
    dependsOn("repack-Jar-With-Dependencies")
}
