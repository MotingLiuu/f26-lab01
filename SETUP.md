# Setup

You need a working Java toolchain and an agent tool (see the course policies for supported options).

## 1. Java and Maven

- Install a JDK, version 21 or newer. Check with `java --version`.
- Install Maven 3.8 or newer. Check with `mvn --version`.

## 2. Build and run the tests

From this directory:

```
mvn test
```

The project should compile and the tests should run. **One test fails on purpose.** You
will diagnose and fix it in Milestone 1. If the build itself fails (nothing compiles, or
Maven cannot download dependencies), sort that out first. (That is an environment
problem, not the seeded bug.)

## 3. Editor

Use any editor or IDE you like. VS Code, IntelliJ IDEA, and Eclipse all import a Maven(Just like cargo in Rust)
project directly. Make sure it recognizes this as a Maven project so you get
compilation and test running from the editor. You can find detailed IDE setup guides
online. Ask us if you are unsure where to get started.

## 4. Agent tool

Configure the agent tool you plan on using for this course, as described on the course
page. You will use it in Milestone 2. Confirm it can see this repository (open this
folder as its working directory, e.g., by `cd`-ing into the repository and running
`claude`, or by opening the Claude Code extension in VS Code, etc.).

One setup item finishes at the end of the lab. Before recitation(Question: what is recitation, 助教带领的习题课，讨论课或辅导课), export a transcript(Question: What is a transcript? 一次完整的与AI的对话记录)
of at least one agent session into `transcripts/` (the handout has the details; do not
commit or push it, since your fork is public). Claude Code users: `./tools/export-transcripts.sh`
does the export for you. Any other tool: the export is your responsibility; you can use
your agent to adapt the script we gave you.

## Getting help

If you run into any setup problems, ask us for help. Come to office hours, or post on Piazza,
or email us. If you wait until recitation to get TA help with environment issues, you may
not be able to finish the engineering portion of this lab before the recitation ends.
