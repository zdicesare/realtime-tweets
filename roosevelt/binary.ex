defmodule Binary do

  def find(value, [], acc, len) do
    {Enum.reverse([value|acc]), len}
  end

  def find(value, [x|xs], acc, len) do
    case x do
      x when x < value ->
        {Enum.reverse([value|acc]) ++ [x|xs], len}
      _ ->
        find(value, xs, [x|acc], len + 1)
    end
  end
end
