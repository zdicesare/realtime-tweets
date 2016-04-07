defmodule Roosevelt.PageController do
  use Roosevelt.Web, :controller

  def index(conn, _params) do
    render conn, "index.html"
  end
end
