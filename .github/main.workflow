workflow "Push" {
  on = "push"
  resolves = ["Check", "Test"]
}

action "Check" {
  uses = "MrRamych/gradle-actions/openjdk-12@2.1"
  args = "check"
  env = {
    GRADLE_USER_HOME = "~/.gradle_tmp"
  }
}

action "Test" {
  uses = "MrRamych/gradle-actions/openjdk-12@2.1"
  args = "test"
  env = {
    GRADLE_USER_HOME = "~/.gradle_tmp"
  }
}
