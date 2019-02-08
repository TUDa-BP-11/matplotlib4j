package com.github.sh0nk.matplotlib4j;

//import com.google.common.base.Strings;
//import com.google.common.collect.Lists;
//import com.google.common.io.Files;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PyCommand {
    private final PythonConfig pythonConfig;

    public PyCommand(PythonConfig pythonConfig) {
        this.pythonConfig = pythonConfig;
    }

//    private final static Logger LOGGER = LoggerFactory.getLogger(PyCommand.class);

    private final static Pattern ERROR_PAT = Pattern.compile("^.+Error:");

    private List<String> buildCommandArgs(String scriptPath) {
        StringBuilder shell = new StringBuilder();
        if (pythonConfig.getPyenv() != null && !pythonConfig.getPyenv().isEmpty()) {
            shell.append("pyenv shell ").append(pythonConfig.getPyenv()).append("; ");

            if (pythonConfig.getVirtualenv() != null && !pythonConfig.getVirtualenv().isEmpty()) {
                shell.append("export PYENV_VIRTUALENV_DISABLE_PROMPT=1; ");
                shell.append("pyenv activate ").append(pythonConfig.getVirtualenv()).append("; ");
            }
            shell.append("python ").append(scriptPath);
        }

        List<String> com;
        if (pythonConfig.getPythonBinPath() != null &&!pythonConfig.getPythonBinPath().isEmpty()) {
            com = new ArrayList();
            com.add(pythonConfig.getPythonBinPath());
            com.add(scriptPath);
//            com = Lists.newArrayList(pythonConfig.getPythonBinPath(), scriptPath);
        } else if (shell.length() != 0) {
            // -l: Use login shell
            com = new ArrayList();
            com.add("bash");
            com.add("-l");
            com.add("-c");
            com.add(shell.toString());
//            com = Lists.newArrayList("bash", "-l", "-c", shell.toString());
        } else {
            // system's default
            com = new ArrayList();
            com.add("python");
            com.add(scriptPath);
//                    Lists.newArrayList("python", scriptPath);
        }

//        LOGGER.debug("Commands... : {}", com);
        return com;
    }

    private void command(List<String> commands) throws IOException, PythonExecutionException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        Process process = pb.start();

        // stdout
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = br.readLine();
        while (line != null) {
            System.out.println(line);
            line = br.readLine();
        }

        // stderr
        // TODO: have a common way with stdout

        br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        line = br.readLine();
        boolean hasError = false;
        while (line != null) {
            sb.append(line).append('\n');
            Matcher matcher = ERROR_PAT.matcher(line);
            if (matcher.find()) {
                hasError = true;
            }
            line = br.readLine();
        }

        String msg = sb.toString();
        if (hasError) {
//            LOGGER.error(msg);
            throw new PythonExecutionException("Python execution error: " + msg);
        } else {
//            LOGGER.warn(msg);
        }
    }

    private void writeFile(String pythonScript, File script) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(script)));
        bw.write(pythonScript);
        bw.close();
    }

    public void execute(String pythonScript) throws IOException, PythonExecutionException {
//        File tmpDir = Files.createTempDir();
        Path tmpDir = Files.createTempDirectory("tmp");
        tmpDir.toFile().deleteOnExit();
//        tmpDir.deleteOnExit();
        File script = new File(tmpDir.toFile(), "exec.py");

        writeFile(pythonScript, script);

        String scriptPath = Paths.get(script.toURI()).toAbsolutePath().toString();
        command(buildCommandArgs(scriptPath));
        tmpDir.toFile().delete();
//        tmpDir.delete();
        
    }
}
