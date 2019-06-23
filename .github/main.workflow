workflow "Push" {
  on = "push"
  resolves = ["Test"]
}

action "Test" {
  uses = "MrRamych/gradle-actions/openjdk-12@2.1"
  args = "test"
  env = {
    GRADLE_USER_HOME = "~/.gradle_tmp"
  }
}
