ExUnit.start

Mix.Task.run "ecto.create", ~w(-r Roosevelt.Repo --quiet)
Mix.Task.run "ecto.migrate", ~w(-r Roosevelt.Repo --quiet)
Ecto.Adapters.SQL.begin_test_transaction(Roosevelt.Repo)

