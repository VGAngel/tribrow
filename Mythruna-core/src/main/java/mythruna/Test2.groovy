package mythruna

println "Testing"

test.add( "A line of text." );


void action( Map args, Closure doIt ) {
    println( "args:" + args );
    println( "doIt:" + doIt );
    
    test.add( doIt );
 
    //println "Calling doIt"    
    //doIt()
}

println "Running action method."

action( test1:"Foo", test2:"Bar" ) {
    println "Inside action"
    println "delegate:" + delegate
    println "owner:" + owner
    println "args:" + args
    
    println "Test:" + test 
} 
