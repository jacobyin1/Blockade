import torch
import random


class QModel:
    def __init__(self, net, epsilon_function):
        self.net = net
        self.train_epochs = 0
        self.e_func = epsilon_function

    def predict(self, state):
        e = self.e_func(self.train_epochs)
        n = random.random()
        if n < e:
            b_i = random.randint(0, 3)
            b = torch.zeros(4)
            b[b_i] = 1
            return b
        with torch.no_grad():
            state1 = state.unsqueeze(0)
            q_sa = self.net(state1).reshape((3, 3))
            v, i = torch.min(q_sa, dim=1)
            a_i = v.argmax()
            a = torch.zeros(4)
            a[a_i] = 1
            return a

    def __loss(self):
        return self

    def train(self):
        self.train_epochs = self.train_epochs + 1
        return self


