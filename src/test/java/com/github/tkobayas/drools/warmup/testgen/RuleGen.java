package com.github.tkobayas.drools.warmup.testgen;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RuleGen {

    public static void main(String[] args) throws Exception {
        
        StringBuilder builder = new StringBuilder();
        
        builder.append("package com.sample\n");
        builder.append("import com.sample.*\n\n");
        builder.append("global java.util.List resultList;\n\n");
        
        for (int i = 0; i < 5000; i++) {
            builder.append("rule \"rule" + i + "\"\n");
            builder.append("  when\n");
            builder.append("    $p : Person( age >= " + i*5 + " && age < " + (i+1)*5 + " )\n");
            builder.append("    $e : Employee( age >= $p.age + " + i + ")\n");
            builder.append("  then\n");
            builder.append("    //System.out.println( kcontext.getRule().getName() + \" : \" + $p );\n");
            builder.append("    resultList.add( kcontext.getRule().getName() + \" : \" + $p );\n");
            builder.append("end\n");
            builder.append("\n");
        }
        
        PrintWriter pr = new PrintWriter(new FileWriter(new File("Sample4.drl")));
        pr.write(builder.toString());
        pr.close();
        
        System.out.println("finish");
    }
}
