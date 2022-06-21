import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

val projectAttributes = mapOf(
    "Main-Class" to null ,
    "Project-Name" to "${projectDir.toString().let{it.substring( it.lastIndexOf("/")+1 )}}" ,
    "Jar-Name" to "${projectDir.toString().let{it.substring( it.lastIndexOf("/")+1 )}}.jar" ,
    "Extracted-Files" to "extractedFiles" ,
    "Output-Path" to "jar"
)

fun download( url : String ) : String {
    val path = "$rootDir/external-downloaded-lib/"
    val fileName = url.substring( url.lastIndexOf( "/" )+1 )
    val file = "$path$fileName"
    if ( File( file ).isFile ) return file
    if ( ! File( path ).isDirectory ) File( path ).mkdirs()
    try {
        URL( url ).openStream().use {
            Channels.newChannel( it ).use { rbc ->
                FileOutputStream( file ).use { fos ->
                    fos.channel.transferFrom( rbc , 0 , Long.MAX_VALUE )

                }
            }
        }
    } catch ( e : Exception ) {
        throw Exception( "Failed To Download Lib : $file" )
    }
    while ( ! File( file ).isFile ) Thread.sleep( 100 )
    return file
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    application
}

repositories { mavenCentral() }

application {
    mainClass.set( projectAttributes["Main-Class"] )
}

dependencies {
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
