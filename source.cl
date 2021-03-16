(defun sum3 [a b   c] (+ a (+ b c)))
(defun sumn (a) (+ a (sumn (- a 1))))
(sum3 10 11 12)