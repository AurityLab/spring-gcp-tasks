workflow "Push" {
  on = "push"
  resolves = ["Check", "Test"]
}

action "Check" {
  uses = "MrRamych/gradle-actions/openjdk-12@2.1"
  args = "check"
}

action "Test" {
  uses = "MrRamych/gradle-actions/openjdk-12@2.1"
  args = "test"
}
