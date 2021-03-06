package com.stateforge.statebuilder.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.stateforge.statebuilder.java.StateBuilderJava;
import com.stateforge.statebuilder.java.StateBuilderJavaApp;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate finite state machine source code from an xml description.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution
 */

public class StateBuilderMojo
extends AbstractMojo
{
    /**
     * The default maven project object
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Location of the file.
     * @parameter default-value="${project.build.directory}/generated-sources/statebuilder"
     */
    private File outputDirectory;


    public StateBuilderMojo() {
        super();
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }


    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Location of the input directory.
     * @parameter default-value="${basedir}/src/main/statemachine"
     */
    private File inputDirectory;


    public File getInputDirectory() {
        return inputDirectory;
    }


    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public void execute()
    throws MojoExecutionException
    {
        File f = outputDirectory;

        getLog().info("Output directory " + outputDirectory);

        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory " + outputDirectory);
            outputDirectory.mkdirs();
        }

        f = inputDirectory;
        getLog().info("Input directory " + f);

        if(f == null){
            getLog().warn("Input directory does not exist");  
            return;
        }

        if ( !inputDirectory.exists() )
        {
            getLog().warn("Input directory " + inputDirectory.getAbsolutePath() + " does not exist");  
            return;
        }

        getLog().info("StateBuidler for Java");
        List<File> stateMachineFiles = getStateMachineFiles();

        for(File stateMachineFile : stateMachineFiles){
            generate(stateMachineFile);        	
        }

        this.project.addCompileSourceRoot( getOutputDirectory().getAbsolutePath());
        getLog().info("Output directory " + getOutputDirectory().getAbsolutePath());
    }

    private void generate(File stateMachineFile) throws MojoExecutionException{
        String args[] = new String[3];
        args[0] = stateMachineFile.getPath();
        args[1] = "-d";
        args[2] = getOutputDirectory().getAbsolutePath();
        
        try {
            int rv = StateBuilderJavaApp.run(args);
            if(rv != 0){
                getLog().error("Error building state machine " + args[0]);
                getLog().error("StateBuilderJava returns " + rv);
                throw new MojoExecutionException("Error building state machine " + args[0]);
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        } catch(Exception e){
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        } 
    }

    public final List<File> getStateMachineFiles()
    {
        List<File> stateMachineFiles = new ArrayList<File>();
        File[] files = getInputDirectory().listFiles( new StateMachineFileFilter( getLog() ) );
        if ( files != null ){
            for ( int i = 0; i < files.length; i++ ) {
                stateMachineFiles.add( files[i] );
            }
        }
        getLog().info(stateMachineFiles.size() + " state machine(s) to generate");
        return stateMachineFiles;
    }

    /**
     * A class used to look up .fsmjava documents from a given directory.
     */
    final class StateMachineFileFilter
    implements FileFilter
    {

        private Log log;

        public StateMachineFileFilter( Log log )
        {
            this.log = log;
        }

        /**
         * Returns true if the file ends with an fsmjava extension.
         * 
         * @param file
         *            The filed being reviewed by the filter.
         * @return true if an fsmjava file.
         */
        public boolean accept( final java.io.File file )
        {
            boolean accept = file.isFile() && file.getName().endsWith( ".fsmjava" );
            if ( log.isDebugEnabled() )
            {
                log.debug( "accept " + accept + " for file " + file.getPath() );
            }
            return accept;
        }
    }
}
