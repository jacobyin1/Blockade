import torch


class QModel:
    def __init__(self, net):
        self.net = net
        self.train_numbers = 0

    def predict(self, state):
        state1 = state.unsqueeze(0)
        q_sa = self.net(state).reshape((3, 3))
        v, i = torch.min(q_sa, dim=1)
        a_i = v.argmax()
        a = torch.zeros(4)
        a[a_i] = 1
        return a

    def target(self):
        return self

    def train(self):
        return self


