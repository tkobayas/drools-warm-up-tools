# drools-warm-up-tools
Tools for Drools kbase warm-up for better performance.
 
## Usage
```
WarmUpHelper helper = new WarmUpHelper();

helper.analyze(kBase); // analyze your kbase first. Mandatory

helper.optimizeAlphaNodeConstraints(); // optimize constraints. Optional

Object[] facts = new Object[FACT_NUM];
for (int i = 0; i < FACT_NUM; i++) {
    facts[i] = new Person("John" + i, i * 5);
}
HashMap<String, Object> globalMap = new HashMap<String, Object>();
globalMap.put("resultList", new ArrayList<String>());
helper.warmUpWithFacts(facts, globalMap); // warm up with prepared facts which fire (most of) all rules. Optional

helper.reviewUnjittedMvelConstraint() // Logs constraints which are not Jitted. So helpful to prepare your warm-up facts. Optional
```

You would call one (or both) of optimizeAlphaNodeConstraints() and warmUpWithFacts() so you can evaluate how warm-up affects.

Please take a look at javadoc comments for further explanation of the methods.