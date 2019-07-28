workflow "Push" {
  on = "push"
  resolves = ["Test"]
}

action "Test" {
  uses = "AurityLab/github-actions-gradle@0.1.0"
  args = "test"
  env = {
    GRADLE_USER_HOME = "~/.gradle_tmp"
  }
}
