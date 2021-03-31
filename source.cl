(def List1 (range 1 10))

(def List2 (list "a" "b" "c" "d"))

(concat List1 List2)


(head List1)

(tail List2)

(get 3 List2)

(defun inc (x) (+ x 1))

(map inc List1)